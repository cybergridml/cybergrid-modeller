#!/bin/bash

# =============================================================================
# CyberGridML - Full Stack Launcher
# =============================================================================
# Starts PostgreSQL, builds the frontend (optional), copies it into the
# backend's static resources, and launches the Spring Boot application.
#
# Usage:
#   ./cybergridml.sh                # start DB + run backend (no frontend rebuild)
#   ./cybergridml.sh -b             # build frontend, copy into backend, then run
#   ./cybergridml.sh --reset-db     # wipe the Postgres volume and recreate it
#   ./cybergridml.sh --stop         # stop the database container
# =============================================================================

set -e

# ─── Configuration ───────────────────────────────────────────────────────────
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

# ─── Java 21 JDK Check ──────────────────────────────────────────────────────
# This project requires Java 21 JDK (with javac) to compile.
# Sirius 2026.7.0 dependencies are compiled with Java 21.
JAVA21_HOME="/usr/lib/jvm/java-21-openjdk-amd64"

if [ -f "$JAVA21_HOME/bin/javac" ]; then
    export JAVA_HOME="$JAVA21_HOME"
    export PATH="$JAVA_HOME/bin:$PATH"
    echo "[INFO]  Using Java 21 JDK: $JAVA_HOME"
elif [ -f "$JAVA21_HOME/bin/java" ]; then
    echo ""
    echo "[ERROR] Java 21 JRE is installed but JDK (javac) is missing!"
    echo ""
    echo "This project requires the Java 21 JDK to compile."
    echo "The Sirius 2026.7.0 libraries are compiled with Java 21."
    echo ""
    echo "To install the Java 21 JDK, run:"
    echo "    sudo apt update && sudo apt install openjdk-21-jdk"
    echo ""
    exit 1
else
    echo ""
    echo "[ERROR] Java 21 JDK is not installed!"
    echo ""
    echo "This project requires the Java 21 JDK to compile."
    echo "The Sirius 2026.7.0 libraries are compiled with Java 21."
    echo ""
    echo "To install the Java 21 JDK, run:"
    echo "    sudo apt update && sudo apt install openjdk-21-jdk"
    echo ""
    exit 1
fi

FRONTEND_DIR="frontend/cybergridml"
BACKEND_DIR="CybergridML"
STATIC_DIR="$BACKEND_DIR/src/main/resources/static"
COMPOSE_FILE="$BACKEND_DIR/docker-compose.yml"

PG_CONTAINER="Nacre_db"
PG_IMAGE="postgres:15"
PG_PORT=5433
PG_USER="dbuser"
PG_PASSWORD="dbpwd"
PG_DB="Nacre-db"

# ─── Color helpers ───────────────────────────────────────────────────────────
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
BOLD='\033[1m'
NC='\033[0m'

info()    { echo -e "${BLUE}[INFO]${NC}  $1"; }
success() { echo -e "${GREEN}[OK]${NC}    $1"; }
warn()    { echo -e "${YELLOW}[WARN]${NC}  $1"; }
error()   { echo -e "${RED}[ERROR]${NC} $1"; }
header()  { echo -e "\n${BOLD}${CYAN}=== $1 ===${NC}\n"; }

# ─── Parse arguments ─────────────────────────────────────────────────────────
BUILD=false
RESET_DB=false
STOP=false

while [[ $# -gt 0 ]]; do
  case "$1" in
    -b|--build)
      BUILD=true
      shift
      ;;
    --reset-db)
      RESET_DB=true
      shift
      ;;
    --stop)
      STOP=true
      shift
      ;;
    -h|--help)
      echo "Usage: ./cybergridml.sh [-b] [--reset-db] [--stop]"
      echo ""
      echo "Options:"
      echo "  -b, --build   Build the frontend and copy it into the backend before running"
      echo "  --reset-db    Wipe the Postgres volume and recreate it fresh"
      echo "  --stop        Stop the database container"
      echo "  -h, --help    Show this help message"
      exit 0
      ;;
    *)
      error "Unknown option: $1"
      echo "Run './cybergridml.sh --help' for usage."
      exit 1
      ;;
  esac
done

check_dependency() {
  if ! command -v "$1" &> /dev/null; then
    error "'$1' is required but not installed."
    exit 1
  fi
}

check_dependency docker
check_dependency npm
check_dependency java

# ─── Stop command ────────────────────────────────────────────────────────────
if [ "$STOP" = true ]; then
  header "Stopping database"
  docker compose -f "$COMPOSE_FILE" stop
  success "Database stopped."
  exit 0
fi

# ─── Reset DB command ────────────────────────────────────────────────────────
if [ "$RESET_DB" = true ]; then
  header "Resetting database"
  warn "This deletes all data in the '$PG_CONTAINER' volume."
  read -p "Continue? [y/N] " -n 1 -r
  echo
  if [[ ! $REPLY =~ ^[Yy]$ ]]; then
    info "Aborted."
    exit 0
  fi
  docker compose -f "$COMPOSE_FILE" down -v
  success "Database and volume removed."
fi

# ─── PostgreSQL ──────────────────────────────────────────────────────────────
# Probe EXACTLY the path the application uses: from the host, through the
# published port, with the real credentials. `docker exec ... pg_isready`
# only proves Postgres is healthy *inside* the container - it says nothing
# about whether the host -> published-port -> container path actually works.
# Prefer a local `psql` (fast, no container spin-up). Spinning a throwaway
# container with --network host can itself hang on locked-down/corporate
# Docker setups, so every probe is wrapped in `timeout` as a hard backstop.
wait_for_postgres() {
  local retries=${1:-30}
  while [ "$retries" -gt 0 ]; do
    if command -v psql &> /dev/null; then
      if PGPASSWORD="$PG_PASSWORD" timeout 5 psql -h 127.0.0.1 -p "$PG_PORT" -U "$PG_USER" -d "$PG_DB" -tc "SELECT 1" > /dev/null 2>&1; then
        return 0
      fi
    else
      if timeout 5 bash -c "exec 3<>/dev/tcp/127.0.0.1/$PG_PORT" 2>/dev/null; then
        return 0
      fi
    fi
    retries=$((retries - 1))
    sleep 1
  done
  return 1
}

start_postgres() {
  header "PostgreSQL"

  docker compose -f "$COMPOSE_FILE" up -d

  info "Waiting for PostgreSQL to be reachable from the host on port $PG_PORT..."
  if wait_for_postgres 30; then
    success "PostgreSQL is ready."
    return
  fi

  warn "Not reachable from the host yet. Restarting the container once..."
  docker restart "$PG_CONTAINER" > /dev/null 2>&1 || true

  if wait_for_postgres 30; then
    success "PostgreSQL is ready after restart."
    return
  fi

  error "PostgreSQL still not reachable from the host after a restart."
  docker compose -f "$COMPOSE_FILE" logs --tail 30
  exit 1
}

# ─── Build frontend and copy into backend ───────────────────────────────────
build_frontend() {
  header "Building frontend"

  info "Installing dependencies..."
  npm install --legacy-peer-deps

  info "Building frontend (npm run build)..."
  npm run build

  info "Copying frontend build into backend static resources..."
  rm -rf "${STATIC_DIR:?}"/*
  mkdir -p "$STATIC_DIR"
  cp -R "$FRONTEND_DIR"/dist/. "$STATIC_DIR"/

  success "Frontend build copied to $STATIC_DIR"
}

# ─── Launch application ─────────────────────────────────────────────────────
launch_app() {
  header "Launching CybergridML backend"
  cd "$BACKEND_DIR"
  JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64 ./mvnw spring-boot:run
}

# ─── Cleanup on exit (Ctrl+C) ────────────────────────────────────────────────
cleanup() {
  echo ""
  warn "Application stopped. The database container is still running."
  info "Run './cybergridml.sh --stop' to stop it."
  exit 0
}
trap cleanup SIGINT SIGTERM

# ─── Main ────────────────────────────────────────────────────────────────────
start_postgres

if [ "$BUILD" = true ]; then
  build_frontend
fi

launch_app
