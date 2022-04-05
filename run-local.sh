#!/usr/bin/env bash

set -e

cd -Pe $(dirname $0)
WORKING_DIR=$(pwd)

usage() {
  cat <<EOF
$1

Usage:

  Commands:
    help      Opens this menu
    clean     Create and populate a new database docker image

EOF
exit 1
}

main() {
  case "$1" in
    help|[-]*help) usage "I cant even with this...";;
    clean|start) createDatabase; shift;;
  esac
  syntheticRecordsBuilder
  loadDatabase $@
}

syntheticRecordsBuilder() {
  cd $WORKING_DIR/docker
  docker build -t vasdvp/health-apis-synthetic-records-builder:local .
  cd $WORKING_DIR
}

createDatabase() {
  echo "stop and remove dqdb"
  docker stop "dqdb" || true && docker rm "dqdb" || true

  #SQL_SERVER_IMAGE=mcr.microsoft.com/mssql/server:2017-latest
  SQL_SERVER_IMAGE=mcr.microsoft.com/azure-sql-edge:latest
  echo "using image $SQL_SERVER_IMAGE"

  # SQL Server Docker Image (you don't have to install anything)
  docker pull $SQL_SERVER_IMAGE

  # Run docker image with local configuration
  [ -f "environments/local.conf" ] && . environments/local.conf
  [ -z "$FLYWAY_PASSWORD" ] && "Help! I can't seem to find my password(FLYWAY_PASSWORD)!" && exit 1
  docker run \
    --name "dqdb" \
    -e 'ACCEPT_EULA=Y' \
    -e "SA_PASSWORD=$FLYWAY_PASSWORD" \
    -p 1433:1433 \
    -d "$SQL_SERVER_IMAGE"

  # Needs time to create SA user
  sleep 10
}

howToBuild() {
  if [ -n "${BUILD_MODE:-}" ]; then echo $BUILD_MODE; return; fi
  if [[ "$(uname)" == *Linux* ]]; then echo docker; return; fi
  echo native
}

buildWithDocker() {
  docker run --rm \
    -e ENVIRONMENT="local" \
    -v $(pwd):/root/synthetic-records \
    -v ~/.m2:/root/.m2 \
    -e NEXUS_USERNAME="${NEXUS_USERNAME}" \
    -e NEXUS_PASSWORD="${NEXUS_PASSWORD}" \
    --network host \
    vasdvp/health-apis-synthetic-records-builder:local \
    ./root/synthetic-records/build.sh $@
}

buildNatively() {
  ENVIRONMENT=local ./build.sh $@
}

loadDatabase() {
  if [ "$(howToBuild)" == "docker" ]
  then
    buildWithDocker $@
  else
    buildNatively $@
  fi
}

main $@
