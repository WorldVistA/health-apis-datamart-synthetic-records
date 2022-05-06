package gov.va.api.health.minimartmanager;

import static com.google.common.base.Preconditions.checkState;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import gov.va.api.health.dataquery.service.controller.allergyintolerance.AllergyIntoleranceEntity;
import gov.va.api.health.dataquery.service.controller.appointment.AppointmentEntity;
import gov.va.api.health.dataquery.service.controller.condition.ConditionEntity;
import gov.va.api.health.dataquery.service.controller.device.DeviceEntity;
import gov.va.api.health.dataquery.service.controller.devicerequest.DeviceRequestEntity;
import gov.va.api.health.dataquery.service.controller.diagnosticreport.DiagnosticReportEntity;
import gov.va.api.health.dataquery.service.controller.encounter.EncounterEntity;
import gov.va.api.health.dataquery.service.controller.etlstatus.LatestResourceEtlStatusEntity;
import gov.va.api.health.dataquery.service.controller.immunization.ImmunizationEntity;
import gov.va.api.health.dataquery.service.controller.location.LocationEntity;
import gov.va.api.health.dataquery.service.controller.medication.MedicationEntity;
import gov.va.api.health.dataquery.service.controller.medicationorder.MedicationOrderEntity;
import gov.va.api.health.dataquery.service.controller.medicationstatement.MedicationStatementEntity;
import gov.va.api.health.dataquery.service.controller.observation.ObservationEntity;
import gov.va.api.health.dataquery.service.controller.organization.OrganizationEntity;
import gov.va.api.health.dataquery.service.controller.patient.PatientEntityV2;
import gov.va.api.health.dataquery.service.controller.practitioner.PractitionerEntity;
import gov.va.api.health.dataquery.service.controller.practitionerrole.PractitionerRoleEntity;
import gov.va.api.health.dataquery.service.controller.practitionerrole.PractitionerRoleSpecialtyMapEntity;
import gov.va.api.health.dataquery.service.controller.procedure.ProcedureEntity;
import gov.va.api.health.vistafhirquery.service.controller.observation.VitalVuidMappingEntity;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import javax.persistence.EntityManager;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;

/**
 * Export a minimart instance to an H2 database at target/mitre.mv.db
 *
 * <p>config/lab.properties is required with connection properties for the source database
 */
@Slf4j
public class H2Exporter {
  private static final List<ExportCriteria> EXPORT_CRITERIA =
      Arrays.asList(
          ExportForPatientCriteria.of(AllergyIntoleranceEntity.class),
          ExportForPatientCriteria.of(AppointmentEntity.class),
          ExportForPatientCriteria.of(ConditionEntity.class),
          ExportForPatientCriteria.of(DeviceEntity.class),
          ExportForPatientCriteria.of(DeviceRequestEntity.class),
          ExportForPatientCriteria.of(DiagnosticReportEntity.class),
          ExportForPatientCriteria.of(EncounterEntity.class),
          ExportForPatientCriteria.of(ImmunizationEntity.class),
          ExportAllCriteria.of(LatestResourceEtlStatusEntity.class),
          ExportAllCriteria.of(LocationEntity.class),
          ExportAllCriteria.of(MedicationEntity.class),
          ExportForPatientCriteria.of(MedicationOrderEntity.class),
          ExportForPatientCriteria.of(MedicationStatementEntity.class),
          ExportForPatientCriteria.of(ObservationEntity.class),
          ExportAllCriteria.of(OrganizationEntity.class),
          ExportForPatientCriteria.of(PatientEntityV2.class),
          ExportAllCriteria.of(PractitionerEntity.class),
          ExportAllCriteria.of(PractitionerRoleEntity.class),
          ExportAllCriteria.of(PractitionerRoleSpecialtyMapEntity.class),
          ExportForPatientCriteria.of(ProcedureEntity.class),
          ExportAllCriteria.of(VitalVuidMappingEntity.class));

  EntityManager minimart;

  EntityManager h2;

  Set<String> includedTypeNames;

  @Builder
  public H2Exporter(String configFile, String outputFile, @NonNull Set<String> includedTypeNames) {
    minimart = new ExternalDb(configFile, managedClasses()).get().createEntityManager();
    h2 = new LocalH2(outputFile, managedClasses()).get().createEntityManager();
    this.includedTypeNames = includedTypeNames;
  }

  public static void main(String[] args) {
    if (args.length != 2) {
      log.error("H2Exporter <application.properties> <h2-database>");
      throw new RuntimeException("Missing arguments");
    }
    var configFile = args[0];
    var outputFile = args[1];
    var includedTypeNames =
        Stream.of(System.getProperty("exporter.included-types", "*").split("\\s*,\\s*"))
            .sorted()
            .distinct()
            .toList();
    log.info("Included types: {}", includedTypeNames);
    H2Exporter.builder()
        .configFile(configFile)
        .outputFile(outputFile)
        .includedTypeNames(Set.copyOf(includedTypeNames))
        .build()
        .export();
    log.info("All done");
  }

  private static List<Class<?>> managedClasses() {
    return EXPORT_CRITERIA.stream().map(ExportCriteria::type).collect(toList());
  }

  public void export() {
    EXPORT_CRITERIA.stream().filter(this::isEnabled).forEach(this::export);
    minimart.close();
    h2.close();
  }

  private void export(ExportCriteria criteria) {
    log.info("Exporting {}", criteria.type());
    h2.getTransaction().begin();
    criteria
        .queries()
        .forEach(
            query -> {
              minimart
                  .createQuery(query, criteria.type())
                  .getResultStream()
                  .forEach(
                      e -> {
                        minimart.detach(e);
                        log.info("{}", e);
                        h2.persist(e);
                      });
            });
    h2.getTransaction().commit();
  }

  private boolean isEnabled(ExportCriteria criteria) {
    var exportedTypeName = criteria.type().getSimpleName();
    return includedTypeNames.contains(exportedTypeName)
        || includedTypeNames.contains(exportedTypeName.replace("Entity", ""));
  }

  private interface ExportCriteria {
    Stream<String> queries();

    Class<?> type();
  }

  @Value
  @AllArgsConstructor(staticName = "of")
  private static class ExportAllCriteria implements ExportCriteria {
    Class<?> type;

    @Override
    public Stream<String> queries() {
      return Stream.of(String.format("select e from %s e", type.getSimpleName()));
    }
  }

  @Value
  @AllArgsConstructor(staticName = "of")
  private static class ExportForPatientCriteria implements ExportCriteria {
    Class<?> type;

    @Override
    public Stream<String> queries() {
      var patientsCsv = System.getProperty("exportPatients");
      checkState(isNotBlank(patientsCsv), "System property exportPatients must be specified");
      return Stream.of(patientsCsv.split(" *, *"))
          .map(
              icn ->
                  String.format(
                      "select e from %s e where e.icn = '%s'", type.getSimpleName(), icn));
    }
  }
}
