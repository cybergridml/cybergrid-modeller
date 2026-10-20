# Prerequisites
The following tools must be installed:
* docker engine
* git
* java (JDK 21 required - upgraded from JDK 17)
* maven
* npm
* an IDE (Eclipse, Intellij, VS Code, ..)

Additionally, you must have a GitHub account.

# Development environment setup
Clone the editor repository, it contains both the frontend and the backend.
```
git clone https://github.com/cybergridml/cybergridmodel.git
```

## Database
The `backend` directory contains a file `docker-compose-dev.yml`. It facilitates launching the database, the data will be kept upon restart.

```
docker compose -f docker-compose-dev.yml up -d
```

## Backend
The backend depends on packages distributed by Obeo. To be allowed to fetch those packages, you must configure maven with your credentials.

**To generate your token:**
* go to your GitHub account
* go to settings and then Developer settings
* go to Personal Access tokens and then Tokens (classic)
* at the top right, click on `Generate new token (classic)`
* only check `read:packagesDownload - packages from GitHub Package Registry`
* copy/paste the token somewhere temporarily

**To feed this token to maven:**

In your home directory, on installation, maven created a `.m2` directory and in there, a `settings.xml` file.

Copy/paste the following configuration, replacing `$GITHUB_USERNAME` with your GitHub username and `$GITHUB_ACCESS_TOKEN` with the token previously generated.

```xml
<?xml version="1.0" encoding="UTF-8"?>
<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"
          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
          xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0 https://maven.apache.org/xsd/settings-1.0.0.xsd">
  <servers>
    <server>
      <id>github-sirius-emfjson</id>
      <username>$GITHUB_USERNAME</username>
      <password>$GITHUB_ACCESS_TOKEN</password>
    </server>
    <server>
      <id>github-flow</id>
      <username>$GITHUB_USERNAME</username>
      <password>$GITHUB_ACCESS_TOKEN</password>
    </server>
    <server>
      <id>github-sirius-components</id>
      <username>$GITHUB_USERNAME</username>
      <password>$GITHUB_ACCESS_TOKEN</password>
    </server>
    <server>
      <id>github-sirius-web</id>
      <username>$GITHUB_USERNAME</username>
      <password>$GITHUB_ACCESS_TOKEN</password>
    </server>
    <server>
      <id>github-syson</id>
      <username>$GITHUB_USERNAME</username>
      <password>$GITHUB_ACCESS_TOKEN</password>
    </server>
  </servers>
</settings>
```

### Starting the application 'CybergridML'

You can also use the capabilities of your IDE.

For Intellij/Eclipse, create a spring boot run configuration for the `CybergridMLApplication`, setting the spring active profile to dev.

Alternatively, the `cybergridml.sh` script at the repository root starts the database and the backend for you (and optionally builds/embeds the frontend). It requires the Java 21 JDK and will fail with an install hint if it is missing.
```
./cybergridml.sh                # start DB + run backend (no frontend rebuild)
./cybergridml.sh -b             # build frontend, copy into backend, then run
./cybergridml.sh --reset-db     # wipe the Postgres volume and recreate it
./cybergridml.sh --stop         # stop the database container
```

## Frontend
### Configuring the token
In your home directory, npm has created a `.npmrc` file.
To feed your token to npm (so that it is allowed to download sirius/syson dependencies), add to this file (replacing `$GITHUB_ACCESS_TOKEN`):
```
//npm.pkg.github.com/:_authToken=$GITHUB_ACCESS_TOKEN
```

