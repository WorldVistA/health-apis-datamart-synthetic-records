#!/usr/bin/env bash

#
# Export a minimart instance to an H2 database at target/mitre.mv.db
# config/lab.properties is required with connection properties for the source database
#
exportH2() {
  local includedTypes="$1"
  cd $BASE_DIR

  LAB_PROPERTIES=config/lab.properties
  [ ! -f "$LAB_PROPERTIES" ] && echo "Missing $LAB_PROPERTIES" && exit 1

  for p in spring.datasource.username spring.datasource.password spring.datasource.url
  do
    ! grep -q "^$p=" $LAB_PROPERTIES && echo "Missing $p in $LAB_PROPERTIES" && exit 1
  done

  [ -f $LOCAL_DB.mv.db ] && rm -v $LOCAL_DB.*

  mvn clean install -q -P'!standard'
  mvn exec:java \
    -Dexec.args="config/lab.properties ./target/mitre" \
    -Dexec.classpathScope="test" \
    -Dexec.cleanupDaemonThreads="false" \
    -Dexec.mainClass="gov.va.api.health.minimartmanager.H2Exporter" \
    -Dexporter.included-types="$includedTypes" \
    -DexportPatients="17,23,32000225,43000199,1017283180V801730" \
    -Djava.util.logging.config.file="nope" \
    -Dorg.jboss.logging.provider="jdk"

  cat<<EOF
Exported H2 database is available: $LOCAL_DB.mv.db
EOF
}

openH2() {
  local db="${1:-}"
  cd $BASE_DIR
  [ -z "${db:-}" ] && db="./target/mitre"
  java -jar ~/.m2/repository/com/h2database/h2/1.4.200/h2-1.4.200.jar -url jdbc:h2:${db} -user sa -password sa
}

usage() {
  cat <<EOF
$0 <command>

Commands:
  export-data-query        Export data-query minimart resources to a local H2 database
  export-vista-fhir-query  Export vista-fhir-query minimart resources to a local H2 database
  open                     Open the local H2 database

Local database: $LOCAL_DB

EOF
exit
}

BASE_DIR=$(dirname $(readlink -f $0))
LOCAL_DB=$BASE_DIR/target/mitre

case $1 in
  export-data-query) exportH2 "AllergyIntolerance,Appointment,Condition,Device,DeviceRequest,DiagnosticReport,Encounter,Immunization,LatestResourceEtlStatus,Location,Medication,MedicationOrder,MedicationStatement,Observation,Organization,PatientV2,Practitioner,PractitionerRole,PractitionerRoleSpecialtyMap,Procedure";;
  export-vista-fhir-query) exportH2 "VitalVuidMapping";;
  open) openH2 "${2:-}";;
  *) usage;;
esac
