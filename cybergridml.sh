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

# ─── OS Detection ───────────────────────────────────────────────────────────
detect_os() {
    case "$(uname -s)" in
        Linux*)
            if [ -f /etc/os-release ]; then
                . /etc/os-release
                case "$ID" in
                    ubuntu|debian|linuxmint|pop) echo "debian" ;;
                    fedora|rhel|centos|rocky|alma) echo "fedora" ;;
                    arch|manjaro) echo "arch" ;;
                    *) echo "linux-unknown" ;;
                esac
            else
                echo "linux-unknown"
            fi
            ;;
        Darwin*) echo "macos" ;;
        MINGW*|MSYS*|CYGWIN*) echo "windows" ;;
        *) echo "unknown" ;;
    esac
}

OS_TYPE=$(detect_os)

# ─── Java 21 JDK Check ──────────────────────────────────────────────────────
get_java21_home() {
    case "$OS_TYPE" in
        debian) echo "/usr/lib/jvm/java-21-openjdk-amd64" ;;
        fedora) echo "/usr/lib/jvm/java-21-openjdk" ;;
        arch) echo "/usr/lib/jvm/java-21-openjdk" ;;
        macos) echo "$(/usr/libexec/java_home -v 21 2>/dev/null || echo '/opt/homebrew/opt/openjdk@21')" ;;
        windows) echo "$PROGRAMFILES/Microsoft/jdk-21" ;;
        *) echo "/usr/lib/jvm/java-21-openjdk-amd64" ;;
    esac
}

JAVA21_HOME=$(get_java21_home)

install_java21() {
    echo ""
    echo "[INFO]  Installing Java 21 JDK for $OS_TYPE..."
    case "$OS_TYPE" in
        debian)
            sudo apt update && sudo apt install -y openjdk-21-jdk
            ;;
        fedora)
            sudo dnf install -y java-21-openjdk-devel
            ;;
        arch)
            sudo pacman -S --noconfirm jdk21-openjdk
            ;;
        macos)
            if ! command -v brew &> /dev/null; then
                echo "[ERROR] Homebrew is required. Install it from https://brew.sh"
                exit 1
            fi
            brew install openjdk@21
            ;;
        windows)
            if command -v winget &> /dev/null; then
                winget install Microsoft.OpenJDK.21
            elif command -v choco &> /dev/null; then
                choco install openjdk21 -y
            else
                echo "[ERROR] Install winget or chocolatey, or download Java 21 manually."
                exit 1
            fi
            ;;
        *)
            echo "[ERROR] Unsupported OS. Please install Java 21 JDK manually."
            exit 1
            ;;
    esac

    JAVA21_HOME=$(get_java21_home)
    if command -v javac &> /dev/null && javac -version 2>&1 | grep -q "21"; then
        echo "[OK]    Java 21 JDK installed successfully."
    elif [ -f "$JAVA21_HOME/bin/javac" ]; then
        echo "[OK]    Java 21 JDK installed successfully."
    else
        echo "[ERROR] Installation failed."
        exit 1
    fi
}

find_java21() {
    if [ -f "$JAVA21_HOME/bin/javac" ]; then
        echo "$JAVA21_HOME"
        return 0
    fi
    if command -v javac &> /dev/null && javac -version 2>&1 | grep -q "21"; then
        dirname "$(dirname "$(command -v javac)")"
        return 0
    fi
    return 1
}

if FOUND_JAVA=$(find_java21); then
    export JAVA_HOME="$FOUND_JAVA"
    export PATH="$JAVA_HOME/bin:$PATH"
    echo "[INFO]  Using Java 21 JDK: $JAVA_HOME"
else
    echo ""
    echo "[WARN]  Java 21 JDK not found. (Detected OS: $OS_TYPE)"
    read -p "Install Java 21 JDK now? [Y/n] " -n 1 -r
    echo
    if [[ ! $REPLY =~ ^[Nn]$ ]]; then
        install_java21
        JAVA21_HOME=$(get_java21_home)
        export JAVA_HOME="$JAVA21_HOME"
        export PATH="$JAVA_HOME/bin:$PATH"
    else
        exit 1
    fi
fi

FRONTEND_DIR="frontend/cybergridml"
BACKEND_DIR="CybergridML"
STATIC_DIR="$BACKEND_DIR/src/main/resources/static"
COMPOSE_FILE="$BACKEND_DIR/docker-compose.yml"

PG_CONTAINER="CybergridML_db"
PG_IMAGE="postgres:15"
PG_PORT=5433
PG_USER="dbuser"
PG_PASSWORD="dbpwd"
PG_DB="CybergridML-db"

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
