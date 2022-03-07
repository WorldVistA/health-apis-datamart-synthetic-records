#!/usr/bin/env bash

REPO="$(cd $(dirname $0)/../ && pwd)"
DATAMART_DIR=$REPO/datamart
MVN_ARGS=
LIGHTHOUSE_SETTINGS=/lighthouse-mvn-settings.xml
if [ -f $LIGHTHOUSE_SETTINGS ]
then
  MVN_ARGS+=" --settings $LIGHTHOUSE_SETTINGS"
  MVN_ARGS+=" -Dhealth-apis-releases.nexus.user=$NEXUS_USERNAME"
  MVN_ARGS+=" -Dhealth-apis-releases.nexus.password=$NEXUS_PASSWORD"
fi

if [ -n "${RESOURCES:-}" ]; then MVN_ARGS+=" -Dresources=$RESOURCES"; fi

cd $REPO/minimart-manager
mvn ${MVN_ARGS:-} -Dimport.directory=$DATAMART_DIR -Dtest=GenerateCsv test
