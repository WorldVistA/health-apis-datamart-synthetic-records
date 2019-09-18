#!/usr/bin/env bash

set -euo pipefail

cd $(readlink -f $(dirname $0))

BASE_DIR=$(pwd)
export PATH=$BASE_DIR:$PATH

#
# Set up a mechanism to communicate job descriptions, etc. so that Jenkins provides more meaningful pages
#
JENKINS_DIR=$BASE_DIR/.jenkins
JENKINS_DESCRIPTION=$JENKINS_DIR/description
JENKINS_BUILD_NAME=$JENKINS_DIR/build-name
[ -d "$JENKINS_DIR" ] && rm -rf "$JENKINS_DIR"
mkdir "$JENKINS_DIR"

#
# To support Jenkins use of the flyway container and because
# the flyway script provided in the flyway docker image isn't executable,
# we'll need to launch flyway via bash.
#
FLYWAY=flyway
if [ -f /flyway/flyway ]; then FLYWAY="bash /flyway/flyway"; fi

#
# To support local testing, add a local install to the front of the path
#
LOCAL_INSTALL=$(find -maxdepth 1 -name "flyway-6*" | sort -V | head -1)
if [ -n "$LOCAL_INSTALL" ]; then export PATH="$LOCAL_INSTALL:$PATH"; fi

#
# Load environment specific configuiration. See local.conf as an example
# of variables that need to be set.
#
echo "Loading configuration for ${ENVIRONMENT}"
case "${ENVIRONMENT}" in
  lab) . environments/lab.conf;;
  staging-lab) . environments/staging-lab.conf;;
  local) . environments/local.conf;;
  *) echo "Unknown environment: $ENVIRONMENT"; exit 1;;
esac

echo "$ENVIRONMENT synthentic database updated" >> $JENKINS_DESCRIPTION
echo "$ENVIRONMENT" >> $JENKINS_BUILD_NAME

#
# These options are true through out the migration.
#
export FLYWAY_EDITION=community
export FLYWAY_DRIVER=com.microsoft.sqlserver.jdbc.SQLServerDriver
export FLYWAY_VALIDATE_ON_MIGRATE=true
export FLYWAY_BASELINE_ON_MIGRATE=true
export FLYWAY_BASELINE_VERSION=0.1
export FLYWAY_PLACEHOLDERS_DB_NAME=dq
export BOOTSTRAP_DB=jenkins

announce() {
  echo "============================================================"
  echo "$1"
  echo "============================================================"
}

#
# When AWS creates the SQL Server instance, this is likely to be the very
# first interaction with it. We won't have a database within the instance yet
# that is required to bootstrap the rest of databases. This hack creates one
# that we will use to track the creation of the other databases.
#
BOOTSTRAP_DB_HACK="IF DB_ID (N'$BOOTSTRAP_DB') IS NULL CREATE DATABASE $BOOTSTRAP_DB"
$FLYWAY info -q -url="${FLYWAY_BASE_URL}" -initSql="$BOOTSTRAP_DB_HACK"

#
# To support local testing, if 'clean' supplied, then the existing
# database will be reset.
#
if [ "${1:-}" == "clean" ]
then
  announce "Cleaning database"
  FORCE_HACK="IF OBJECT_ID('[dbo].[flyway_schema_history_cleaner]','U') IS NOT NULL DELETE FROM [dbo].[flyway_schema_history_cleaner];"
  $FLYWAY migrate \
    -url="${FLYWAY_BASE_URL};databaseName=$BOOTSTRAP_DB" \
    -table=flyway_schema_history_cleaner \
    -locations='filesystem:db/destroyer' \
    -initSql="$FORCE_HACK"
fi


#
# Bootstrap the database
#
announce "Bootstrapping database"
$FLYWAY migrate \
    -url="${FLYWAY_BASE_URL};databaseName=$BOOTSTRAP_DB" \
    -table=flyway_schema_history \
    -locations='filesystem:db/bootstrap'

#
# Now apply migrations
#
announce "Migrating database"
$FLYWAY migrate \
    -url="${FLYWAY_BASE_URL};databaseName=$FLYWAY_PLACEHOLDERS_DB_NAME" \
    -table=flyway_schema_history \
    -locations='filesystem:db/migration' \
    -schemas=app

#
# Populate database
#
DQ_TAR=$BASE_DIR/data-query.tar.gz
fetch-data-query $DQ_TAR
populate-db $DQ_TAR
