package gov.va.api.health.minimartmanager.minimart;

import static com.google.common.base.Preconditions.checkState;
import static gov.va.api.health.dataquery.service.controller.Transformers.isBlank;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.trimToEmpty;
import static org.apache.commons.lang3.StringUtils.trimToNull;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import gov.va.api.health.autoconfig.configuration.JacksonConfig;
import gov.va.api.health.dataquery.service.controller.CompositeCdwIds;
import gov.va.api.health.dataquery.service.controller.allergyintolerance.AllergyIntoleranceEntity;
import gov.va.api.health.dataquery.service.controller.allergyintolerance.DatamartAllergyIntolerance;
import gov.va.api.health.dataquery.service.controller.appointment.AppointmentEntity;
import gov.va.api.health.dataquery.service.controller.appointment.DatamartAppointment;
import gov.va.api.health.dataquery.service.controller.condition.ConditionEntity;
import gov.va.api.health.dataquery.service.controller.condition.DatamartCondition;
import gov.va.api.health.dataquery.service.controller.device.DatamartDevice;
import gov.va.api.health.dataquery.service.controller.device.DeviceEntity;
import gov.va.api.health.dataquery.service.controller.devicerequest.DatamartDeviceRequest;
import gov.va.api.health.dataquery.service.controller.devicerequest.DeviceRequestEntity;
import gov.va.api.health.dataquery.service.controller.diagnosticreport.DatamartDiagnosticReport;
import gov.va.api.health.dataquery.service.controller.diagnosticreport.DiagnosticReportEntity;
import gov.va.api.health.dataquery.service.controller.encounter.DatamartEncounter;
import gov.va.api.health.dataquery.service.controller.encounter.EncounterEntity;
import gov.va.api.health.dataquery.service.controller.etlstatus.LatestResourceEtlStatusEntity;
import gov.va.api.health.dataquery.service.controller.immunization.DatamartImmunization;
import gov.va.api.health.dataquery.service.controller.immunization.ImmunizationEntity;
import gov.va.api.health.dataquery.service.controller.location.DatamartLocation;
import gov.va.api.health.dataquery.service.controller.location.LocationEntity;
import gov.va.api.health.dataquery.service.controller.medication.DatamartMedication;
import gov.va.api.health.dataquery.service.controller.medication.MedicationEntity;
import gov.va.api.health.dataquery.service.controller.medicationorder.DatamartMedicationOrder;
import gov.va.api.health.dataquery.service.controller.medicationorder.MedicationOrderEntity;
import gov.va.api.health.dataquery.service.controller.medicationstatement.DatamartMedicationStatement;
import gov.va.api.health.dataquery.service.controller.medicationstatement.MedicationStatementEntity;
import gov.va.api.health.dataquery.service.controller.observation.DatamartObservation;
import gov.va.api.health.dataquery.service.controller.observation.ObservationEntity;
import gov.va.api.health.dataquery.service.controller.organization.DatamartOrganization;
import gov.va.api.health.dataquery.service.controller.organization.OrganizationEntity;
import gov.va.api.health.dataquery.service.controller.patient.DatamartPatient;
import gov.va.api.health.dataquery.service.controller.patient.PatientEntityV2;
import gov.va.api.health.dataquery.service.controller.practitioner.DatamartPractitioner;
import gov.va.api.health.dataquery.service.controller.practitioner.PractitionerEntity;
import gov.va.api.health.dataquery.service.controller.practitionerrole.DatamartPractitionerRole;
import gov.va.api.health.dataquery.service.controller.practitionerrole.PractitionerRoleEntity;
import gov.va.api.health.dataquery.service.controller.practitionerrole.PractitionerRoleSpecialtyMapEntity;
import gov.va.api.health.dataquery.service.controller.practitionerrole.SpecialtyMapCompositeId;
import gov.va.api.health.dataquery.service.controller.procedure.DatamartProcedure;
import gov.va.api.health.dataquery.service.controller.procedure.ProcedureEntity;
import gov.va.api.health.minimartmanager.ExternalDb;
import gov.va.api.health.minimartmanager.LatestResourceEtlStatusUpdater;
import gov.va.api.health.minimartmanager.LocalH2;
import gov.va.api.health.vistafhirquery.service.controller.observation.VitalVuidMappingCompositeId;
import gov.va.api.health.vistafhirquery.service.controller.observation.VitalVuidMappingEntity;
import gov.va.api.lighthouse.datamart.CompositeCdwId;
import gov.va.api.lighthouse.datamart.CompositeIdDatamartEntity;
import gov.va.api.lighthouse.datamart.DatamartEntity;
import gov.va.api.lighthouse.datamart.DatamartReference;
import gov.va.api.lighthouse.datamart.HasReplaceableId;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaDelete;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.StringUtils;

@Slf4j
public class MitreMinimartMaker {
  private static final ObjectMapper MAPPER = JacksonConfig.createMapper();

  private static final List<Class<?>> MANAGED_CLASSES =
      Arrays.asList(
          AllergyIntoleranceEntity.class,
          AppointmentEntity.class,
          ConditionEntity.class,
          DeviceEntity.class,
          DeviceRequestEntity.class,
          DiagnosticReportEntity.class,
          EncounterEntity.class,
          ImmunizationEntity.class,
          LatestResourceEtlStatusEntity.class,
          LocationEntity.class,
          MedicationOrderEntity.class,
          MedicationEntity.class,
          MedicationStatementEntity.class,
          ObservationEntity.class,
          OrganizationEntity.class,
          PatientEntityV2.class,
          PractitionerEntity.class,
          PractitionerRoleEntity.class,
          PractitionerRoleSpecialtyMapEntity.class,
          ProcedureEntity.class,
          VitalVuidMappingEntity.class);

  private final ThreadLocal<EntityManager> LOCAL_ENTITY_MANAGER = new ThreadLocal<>();

  private final Function<DatamartAllergyIntolerance, AllergyIntoleranceEntity>
      toAllergyIntoleranceEntity =
          dm -> {
            CompositeCdwId compositeCdwId = CompositeCdwId.fromCdwId(dm.cdwId());
            return AllergyIntoleranceEntity.builder()
                .cdwId(dm.cdwId())
                .cdwIdNumber(compositeCdwId.cdwIdNumber())
                .cdwIdResourceCode(compositeCdwId.cdwIdResourceCode())
                .icn(patientIcn(dm.patient()))
                .payload(datamartToString(dm))
                .build();
          };

  private final Function<DatamartAppointment, AppointmentEntity> toAppointmentEntity =
      dm -> {
        Instant lastUpdated =
            dm.end().isPresent() ? dm.end().get().plus(30, ChronoUnit.DAYS) : Instant.now();
        checkState(dm.lastUpdated() == null);
        dm.lastUpdated(lastUpdated.truncatedTo(ChronoUnit.MILLIS));
        CompositeCdwId compositeCdwId = CompositeCdwId.fromCdwId(dm.cdwId());
        return AppointmentEntity.builder()
            .cdwIdNumber(compositeCdwId.cdwIdNumber())
            .cdwIdResourceCode(compositeCdwId.cdwIdResourceCode())
            .icn(
                dm.participant().stream()
                    .filter(p -> "PATIENT".equalsIgnoreCase(p.type().orElse(null)))
                    .findFirst()
                    .map(this::patientIcn)
                    .orElseThrow(
                        () -> new IllegalStateException("Cannot find PATIENT participant")))
            .locationSid(
                dm.participant().stream()
                    .filter(p -> "LOCATION".equalsIgnoreCase(p.type().orElse(null)))
                    .findFirst()
                    .map(
                        l -> {
                          var maybeRef = l.reference();
                          if (maybeRef.isPresent()) {
                            return CompositeCdwId.fromCdwId(maybeRef.get())
                                .cdwIdNumber()
                                .intValueExact();
                          }
                          return null;
                        })
                    .orElse(null))
            .date(dm.start().orElse(null))
            .lastUpdated(dm.lastUpdated())
            .payload(datamartToString(dm))
            .build();
      };

  private final Function<DatamartCondition, ConditionEntity> toConditionEntity =
      dm -> {
        CompositeCdwId compositeCdwId = CompositeCdwId.fromCdwId(dm.cdwId());
        return ConditionEntity.builder()
            .cdwIdNumber(compositeCdwId.cdwIdNumber())
            .cdwIdResourceCode(compositeCdwId.cdwIdResourceCode())
            .icn(patientIcn(dm.patient()))
            .category(jsonValue(dm.category()))
            .clinicalStatus(jsonValue(dm.clinicalStatus()))
            .payload(datamartToString(dm))
            .build();
      };

  private final Function<DatamartDevice, DeviceEntity> toDeviceEntity =
      dm -> {
        CompositeCdwId compositeCdwId =
            CompositeCdwIds.optionalFromCdwId(dm.cdwId())
                .orElseGet(() -> CompositeCdwId.fromCdwId(dm.cdwId() + ":I"));
        return DeviceEntity.builder()
            .cdwId(dm.cdwId())
            .cdwIdNumber(compositeCdwId.cdwIdNumber())
            .cdwIdResourceCode(compositeCdwId.cdwIdResourceCode())
            .icn(dm.patient().reference().orElse(null))
            .lastUpdated(Instant.now())
            .payload(datamartToString(dm))
            .build();
      };

  private final Function<DatamartDeviceRequest, DeviceRequestEntity> toDeviceRequestEntity =
      dm -> {
        CompositeCdwId compositeCdwId = CompositeCdwId.fromCdwId(dm.cdwId());
        return DeviceRequestEntity.builder()
            .cdwIdNumber(compositeCdwId.cdwIdNumber())
            .cdwIdResourceCode(compositeCdwId.cdwIdResourceCode())
            .icn(dm.patient().reference().orElse(null))
            .date(dm.occurrenceDateTime().orElse(null))
            .lastUpdated(Instant.now())
            .payload(datamartToString(dm))
            .build();
      };

  private final Function<DatamartDiagnosticReport, DiagnosticReportEntity>
      toDiagnosticReportEntity =
          dm -> {
            CompositeCdwId compositeCdwId = CompositeCdwId.fromCdwId(dm.cdwId());
            return DiagnosticReportEntity.builder()
                .cdwIdNumber(compositeCdwId.cdwIdNumber())
                .cdwIdResourceCode(compositeCdwId.cdwIdResourceCode())
                .icn(dm.patient().reference().orElse(null))
                .category("CH")
                .code("panel")
                .dateUtc(Instant.parse(dm.issuedDateTime()))
                .lastUpdated(null)
                .payload(datamartToString(dm))
                .build();
          };

  private final Function<DatamartEncounter, EncounterEntity> toEncounterEntity =
      dm -> {
        CompositeCdwId compositeCdwId = CompositeCdwId.fromCdwId(dm.cdwId());
        Instant start =
            dm.period()
                .map(p -> p.start().orElse(null))
                .map(s -> s.atStartOfDay().toInstant(ZoneOffset.UTC))
                .orElse(null);
        Instant end =
            dm.period()
                .map(p -> p.end().orElse(null))
                .map(e -> e.atStartOfDay().toInstant(ZoneOffset.UTC))
                .orElse(null);
        return EncounterEntity.builder()
            .cdwIdNumber(compositeCdwId.cdwIdNumber())
            .cdwIdResourceCode(compositeCdwId.cdwIdResourceCode())
            .icn(patientIcn(dm.patient()))
            .startDateTime(start)
            .endDateTime(end)
            .lastUpdated(Instant.now())
            .payload(datamartToString(dm))
            .build();
      };

  private final Function<DatamartImmunization, ImmunizationEntity> toImmunizationEntity =
      dm -> {
        CompositeCdwId compositeCdwId = CompositeCdwId.fromCdwId(dm.cdwId());
        return ImmunizationEntity.builder()
            .cdwIdNumber(compositeCdwId.cdwIdNumber())
            .cdwIdResourceCode(compositeCdwId.cdwIdResourceCode())
            .icn(patientIcn(dm.patient()))
            .payload(datamartToString(dm))
            .build();
      };

  private final Function<DatamartLocation, LocationEntity> toLocationEntity =
      dm -> {
        CompositeCdwId compositeCdwId = CompositeCdwId.fromCdwId(dm.cdwId());
        Optional<CompositeCdwId> orgCompositeId =
            Optional.ofNullable(dm.managingOrganization())
                .map(org -> org.reference().orElse(null))
                .map(ref -> CompositeCdwId.fromCdwId(ref));
        Integer managingOrgIdNumber =
            orgCompositeId.map(id -> id.cdwIdNumber().intValueExact()).orElse(null);
        Character managingOrgResourceCode =
            orgCompositeId.map(id -> id.cdwIdResourceCode()).orElse(null);
        return LocationEntity.builder()
            .cdwId(dm.cdwId())
            .cdwIdNumber(compositeCdwId.cdwIdNumber())
            .cdwIdResourceCode(compositeCdwId.cdwIdResourceCode())
            .name(dm.name())
            .street(dm.address().line1())
            .city(dm.address().city())
            .state(dm.address().state())
            .postalCode(dm.address().postalCode())
            .stationNumber(dm.facilityId().map(fid -> fid.stationNumber()).orElse(null))
            .facilityType(
                dm.facilityId().map(fid -> fid.type()).map(type -> type.toString()).orElse(null))
            .managingOrgIdNumber(managingOrgIdNumber)
            .managingOrgResourceCode(managingOrgResourceCode)
            .locationIen(dm.locationIen().orElse(null))
            .payload(datamartToString(dm))
            .build();
      };

  private final Function<DatamartMedication, MedicationEntity> toMedicationEntity =
      dm -> {
        CompositeCdwId compositeCdwId = CompositeCdwId.fromCdwId(dm.cdwId());
        return MedicationEntity.builder()
            .cdwIdNumber(compositeCdwId.cdwIdNumber())
            .cdwIdResourceCode(compositeCdwId.cdwIdResourceCode())
            .payload(datamartToString(dm))
            .build();
      };

  private final Function<DatamartMedicationOrder, MedicationOrderEntity> toMedicationOrderEntity =
      dm -> {
        CompositeCdwId compositeCdwId = CompositeCdwId.fromCdwId(dm.cdwId());
        return MedicationOrderEntity.builder()
            .cdwIdNumber(compositeCdwId.cdwIdNumber())
            .cdwIdResourceCode(compositeCdwId.cdwIdResourceCode())
            .icn(patientIcn(dm.patient()))
            .payload(datamartToString(dm))
            .build();
      };

  private final Function<DatamartMedicationStatement, MedicationStatementEntity>
      toMedicationStatementEntity =
          dm -> {
            CompositeCdwId compositeCdwId = CompositeCdwId.fromCdwId(dm.cdwId());
            return MedicationStatementEntity.builder()
                .cdwIdNumber(compositeCdwId.cdwIdNumber())
                .cdwIdResourceCode(compositeCdwId.cdwIdResourceCode())
                .icn(patientIcn(dm.patient()))
                .payload(datamartToString(dm))
                .build();
          };

  private final Function<DatamartObservation, ObservationEntity> toObservationEntity =
      dm -> {
        Instant lastUpdated =
            dm.effectiveDateTime().isPresent()
                ? dm.effectiveDateTime().get().plus(30, ChronoUnit.DAYS)
                : Instant.now();
        checkState(dm.lastUpdated() == null);
        dm.lastUpdated(lastUpdated.truncatedTo(ChronoUnit.MILLIS));
        CompositeCdwId compositeCdwId = CompositeCdwId.fromCdwId(dm.cdwId());
        return ObservationEntity.builder()
            .cdwIdNumber(compositeCdwId.cdwIdNumber())
            .cdwIdResourceCode(compositeCdwId.cdwIdResourceCode())
            .icn(dm.subject().isPresent() ? patientIcn(dm.subject().get()) : null)
            .lastUpdated(dm.lastUpdated())
            .dateUtc(dm.effectiveDateTime().orElse(null))
            .payload(datamartToString(dm))
            .category(jsonValue(dm.category()))
            .code(
                dm.code().isPresent() && dm.code().get().coding().isPresent()
                    ? dm.code().get().coding().get().code().orElse(null)
                    : null)
            .build();
      };

  private final Function<DatamartOrganization, OrganizationEntity> toOrganizationEntity =
      dm -> {
        CompositeCdwId compositeCdwId = CompositeCdwId.fromCdwId(dm.cdwId());
        var address =
            Optional.ofNullable(dm.address())
                .orElse(DatamartOrganization.Address.builder().build());
        return OrganizationEntity.builder()
            .cdwId(dm.cdwId())
            .cdwIdNumber(compositeCdwId.cdwIdNumber())
            .cdwIdResourceCode(compositeCdwId.cdwIdResourceCode())
            .npi(dm.npi().orElse(null))
            .name(dm.name())
            .street(trimToNull(trimToEmpty(address.line1()) + " " + trimToEmpty(address.line2())))
            .city(address.city())
            .state(address.state())
            .postalCode(address.postalCode())
            .facilityType(
                dm.facilityId().map(fid -> fid.type()).map(type -> type.toString()).orElse(null))
            .stationNumber(dm.facilityId().map(fid -> fid.stationNumber()).orElse(null))
            .lastUpdated(Instant.now())
            .payload(datamartToString(dm))
            .build();
      };

  private final Function<DatamartPatient, PatientEntityV2> toPatientEntity =
      dm ->
          PatientEntityV2.builder()
              .icn(dm.fullIcn())
              .fullName(dm.name())
              .lastName(dm.lastName())
              .firstName(dm.firstName())
              .birthDate(Instant.parse(dm.birthDateTime()))
              .gender(dm.gender())
              .ssn(dm.ssn())
              .managingOrganization(dm.managingOrganization().orElse(null))
              .payload(datamartToString(dm))
              .build();

  private final Function<DatamartPractitioner, PractitionerEntity> toPractitionerEntity =
      dm -> {
        CompositeCdwId compositeCdwId = CompositeCdwId.fromCdwId(dm.cdwId());
        return PractitionerEntity.builder()
            .cdwIdNumber(compositeCdwId.cdwIdNumber())
            .cdwIdResourceCode(compositeCdwId.cdwIdResourceCode())
            .icn(dm.fullIcn())
            .npi(dm.npi().orElse(null))
            .familyName(dm.name().family())
            .givenName(dm.name().given())
            .fullName(dm.name().family() + "," + dm.name().given())
            .payload(datamartToString(dm))
            .build();
      };

  private final Function<DatamartPractitionerRole, PractitionerRoleEntity>
      toPractitionerRoleEntity =
          dm -> {
            CompositeCdwId compositeCdwId = CompositeCdwId.fromCdwId(dm.cdwId());
            CompositeCdwId practitionerCdwId =
                CompositeCdwId.fromCdwId(dm.practitioner().get().reference().get());
            String fullName = dm.practitioner().get().display().get();
            List<String> names =
                Arrays.stream(fullName.split(",", -1))
                    .map(StringUtils::trimToNull)
                    .filter(Objects::nonNull)
                    .collect(toList());
            checkState(names.size() == 2);
            return PractitionerRoleEntity.builder()
                .cdwIdNumber(compositeCdwId.cdwIdNumber())
                .cdwIdResourceCode(compositeCdwId.cdwIdResourceCode())
                .practitionerIdNumber(practitionerCdwId.cdwIdNumber())
                .practitionerResourceCode(practitionerCdwId.cdwIdResourceCode())
                .givenName(names.get(1))
                .familyName(names.get(0))
                .fullName(fullName)
                .npi(dm.npi().orElse(null))
                .active(dm.active())
                .lastUpdated(Instant.now())
                .payload(datamartToString(dm))
                .build();
          };

  private final Function<DatamartPractitionerRole, List<PractitionerRoleSpecialtyMapEntity>>
      toPractitionerRoleSpecialtyMapEntities =
          dm -> {
            CompositeCdwId compositeCdwId = CompositeCdwId.fromCdwId(dm.cdwId());
            return dm.specialty().stream()
                .map(
                    datamartSpecialty -> {
                      if (!isBlank(datamartSpecialty.x12Code())) {
                        return datamartSpecialty.x12Code().get();
                      } else if (!isBlank(datamartSpecialty.vaCode())) {
                        return datamartSpecialty.vaCode().get();
                      } else if (!isBlank(datamartSpecialty.specialtyCode())) {
                        return datamartSpecialty.specialtyCode().get();
                      }
                      return null;
                    })
                .filter(Objects::nonNull)
                .map(
                    specialtyCode ->
                        PractitionerRoleSpecialtyMapEntity.builder()
                            .practitionerRoleIdNumber(compositeCdwId.cdwIdNumber())
                            .practitionerRoleResourceCode(compositeCdwId.cdwIdResourceCode())
                            .specialtyCode(specialtyCode)
                            .build())
                .collect(toList());
          };

  private final Function<DatamartProcedure, ProcedureEntity> toProcedure =
      dm -> {
        CompositeCdwId compositeCdwId = CompositeCdwId.fromCdwId(dm.cdwId());
        Instant lastUpdated =
            dm.performedDateTime().isPresent()
                ? dm.performedDateTime().get().plus(30, ChronoUnit.DAYS)
                : Instant.now();
        checkState(dm.lastUpdated() == null);
        dm.lastUpdated(lastUpdated.truncatedTo(ChronoUnit.MILLIS));
        Long performedOnEpoch =
            dm.performedDateTime().isPresent() ? dm.performedDateTime().get().toEpochMilli() : null;
        return ProcedureEntity.builder()
            .cdwId(dm.cdwId())
            .cdwIdNumber(compositeCdwId.cdwIdNumber())
            .cdwIdResourceCode(compositeCdwId.cdwIdResourceCode())
            .icn(patientIcn(dm.patient()))
            .lastUpdated(dm.lastUpdated())
            .performedOnEpochTime(performedOnEpoch)
            .payload(datamartToString(dm))
            .build();
      };

  private final Function<CSVRecord, VitalVuidMappingEntity> toVitalVuidMapping =
      csvRecord ->
          VitalVuidMappingEntity.builder()
              .codingSystemId(Short.valueOf(csvRecord.get("CodingSystemID")))
              .sourceValue(csvRecord.get("SourceValue"))
              .code(csvRecord.get("Code"))
              .display(csvRecord.get("Display"))
              .uri(csvRecord.get("URI"))
              .build();

  private int totalRecords;

  private String resourceToSync;

  private EntityManagerFactory entityManagerFactory;

  private List<EntityManager> entityManagers;

  private AtomicInteger addedCount = new AtomicInteger(0);

  private MitreMinimartMaker(String resourceToSync, String configFile) {
    this.resourceToSync = resourceToSync;
    if (configFile == null || configFile.isBlank()) {
      log.info("No config file was specified... Defaulting to local h2 database...");
      entityManagerFactory = new LocalH2("./target/minimart", MANAGED_CLASSES).get();
    } else {
      entityManagerFactory = new ExternalDb(configFile, MANAGED_CLASSES).get();
    }
    entityManagers = Collections.synchronizedList(new ArrayList<>());
  }

  public static void removeOldEntities(
      String configFile, Collection<Consumer<EntityManager>> entitiesForRemoval) {
    MitreMinimartMaker mmm = new MitreMinimartMaker(null, configFile);
    log.info("Removing old entities...");
    entitiesForRemoval.forEach(r -> r.accept(mmm.getEntityManager()));
    log.info("Removed.");
    mmm.cleanUpEntityManagers();
  }

  public static void sync(String directory, String resourceToSync, String configFile) {
    MitreMinimartMaker mmm = new MitreMinimartMaker(resourceToSync, configFile);
    log.info("Syncing {} files in {} to db", mmm.resourceToSync, directory);
    mmm.pushToDatabaseByResourceType(directory);
    log.info("{} sync complete", mmm.resourceToSync);
  }

  private void cleanUpEntityManagers() {
    /*
     * Commit and clean up the transactions for the entity managers from
     * the various threads.
     */
    for (EntityManager entityManager : entityManagers) {
      entityManager.getTransaction().commit();
      entityManager.close();
      // HACK
      LOCAL_ENTITY_MANAGER.remove();
    }
    entityManagers.clear();
  }

  @SneakyThrows
  private String datamartToString(HasReplaceableId e) {
    return MAPPER.writeValueAsString(e);
  }

  public int deleteAllSpecialtiesByPractitionerRole(CompositeCdwId praRolCdwId) {
    EntityManager em = getEntityManager();
    CriteriaBuilder builder = em.getCriteriaBuilder();
    CriteriaDelete<PractitionerRoleSpecialtyMapEntity> query =
        builder.createCriteriaDelete(PractitionerRoleSpecialtyMapEntity.class);
    var rootEntry = query.from(PractitionerRoleSpecialtyMapEntity.class);
    query.where(
        builder.and(
            builder.equal(rootEntry.get("practitionerRoleIdNumber"), praRolCdwId.cdwIdNumber()),
            builder.equal(
                rootEntry.get("practitionerRoleResourceCode"), praRolCdwId.cdwIdResourceCode())));
    return em.createQuery(query).executeUpdate();
  }

  @SneakyThrows
  private String fileToString(File file) {
    return new String(Files.readAllBytes(Paths.get(file.getPath())));
  }

  private Stream<File> findUniqueFiles(File dmDirectory, String filePattern) {
    var uniqueFiles = MakerUtils.findUniqueFiles(dmDirectory, filePattern);
    totalRecords = uniqueFiles.size();
    return uniqueFiles.stream();
  }

  private EntityManager getEntityManager() {
    if (LOCAL_ENTITY_MANAGER.get() == null) {
      LOCAL_ENTITY_MANAGER.set(entityManagerFactory.createEntityManager());
      LOCAL_ENTITY_MANAGER.get().getTransaction().begin();
      entityManagers.add(LOCAL_ENTITY_MANAGER.get());
    }
    return LOCAL_ENTITY_MANAGER.get();
  }

  @SneakyThrows
  private void insertByPractitionerRoleSpecialtyMapping(File file) {
    DatamartPractitionerRole dm = MAPPER.readValue(file, DatamartPractitionerRole.class);
    var entities = toPractitionerRoleSpecialtyMapEntities.apply(dm);
    entities.stream()
        .parallel()
        .forEach(
            entity -> {
              var pk =
                  SpecialtyMapCompositeId.builder()
                      .practitionerRoleIdNumber(entity.practitionerRoleIdNumber())
                      .practitionerRoleResourceCode(entity.practitionerRoleResourceCode())
                      .specialtyCode(entity.specialtyCode())
                      .build();
              save(entity, pk);
            });
  }

  @SneakyThrows
  private void insertResourceByPattern(
      File dmDirectory, String filePattern, Consumer<File> fileWriter) {
    findUniqueFiles(dmDirectory, filePattern).parallel().forEach(fileWriter);
  }

  @SneakyThrows
  <E extends Enum<E>> String jsonValue(E e) {
    JsonProperty jsonProperty = e.getClass().getField(e.name()).getAnnotation(JsonProperty.class);
    if (jsonProperty != null && jsonProperty.value() != null) {
      return jsonProperty.value();
    }
    return e.toString();
  }

  private String patientIcn(DatamartReference dm) {
    if (dm != null && dm.reference().isPresent()) {
      return dm.reference().get().replaceAll("http.*/fhir/v0/dstu2/Patient/", "");
    }
    return null;
  }

  private <T> void postPractitionerRole(PractitionerRoleEntity entity) {
    var pk = entity.compositeCdwId();
    deleteAllSpecialtiesByPractitionerRole(pk);
  }

  private void pushToDatabaseByResourceType(String directory) {
    File dmDirectory = new File(directory);
    DatabaseLoader loader = new DatabaseLoader(dmDirectory);
    if (dmDirectory.listFiles() == null) {
      log.error("No files in directory {}", directory);
      throw new RuntimeException("No files found in directory: " + directory);
    }
    switch (resourceToSync) {
      case "AllergyIntolerance":
        loader.insertResourceByType(DatamartAllergyIntolerance.class, toAllergyIntoleranceEntity);
        break;
      case "Appointment":
        loader.insertResourceByType(DatamartAppointment.class, toAppointmentEntity);
        break;
      case "Condition":
        loader.insertResourceByType(DatamartCondition.class, toConditionEntity);
        break;
      case "Device":
        loader.insertResourceByType(DatamartDevice.class, toDeviceEntity);
        break;
      case "DeviceRequest":
        loader.insertResourceByType(DatamartDeviceRequest.class, toDeviceRequestEntity);
        break;
      case "DiagnosticReport":
        loader.insertResourceByType(DatamartDiagnosticReport.class, toDiagnosticReportEntity);
        break;
      case "Encounter":
        loader.insertResourceByType(DatamartEncounter.class, toEncounterEntity);
        break;
      case "Immunization":
        loader.insertResourceByType(DatamartImmunization.class, toImmunizationEntity);
        break;
      case "Location":
        loader.insertResourceByType(DatamartLocation.class, toLocationEntity);
        break;
      case "Medication":
        loader.insertResourceByType(DatamartMedication.class, toMedicationEntity);
        break;
      case "MedicationOrder":
        loader.insertResourceByType(DatamartMedicationOrder.class, toMedicationOrderEntity);
        break;
      case "MedicationStatement":
        loader.insertResourceByType(DatamartMedicationStatement.class, toMedicationStatementEntity);
        break;
      case "Observation":
        loader.insertResourceByType(DatamartObservation.class, toObservationEntity);
        break;
      case "Organization":
        loader.insertResourceByType(DatamartOrganization.class, toOrganizationEntity);
        break;
      case "Patient":
        loader.insertResourceByType(DatamartPatient.class, toPatientEntity);
        break;
      case "Practitioner":
        loader.insertResourceByType(DatamartPractitioner.class, toPractitionerEntity);
        break;
      case "PractitionerRole":
        loader.insertResourceByType(
            DatamartPractitionerRole.class, toPractitionerRoleEntity, this::postPractitionerRole);
        break;
      case "PractitionerRoleSpecialtyMap":
        insertResourceByPattern(
            dmDirectory,
            DatamartFilenamePatterns.get().json(DatamartPractitionerRole.class),
            this::insertByPractitionerRoleSpecialtyMapping);
        break;
      case "Procedure":
        loader.insertResourceByType(DatamartProcedure.class, toProcedure);
        break;
      case "VitalVuidMapping":
        loader.insertByCsvColumnNames(
            "db-dumps/VistaVuidVitalsMapping.csv",
            toVitalVuidMapping,
            e ->
                VitalVuidMappingCompositeId.builder()
                    .sourceValue(e.sourceValue())
                    .code(e.code())
                    .codingSystemId(e.codingSystemId())
                    .uri(e.uri())
                    .build());
        break;
      default:
        throw new RuntimeException("Couldn't determine resource type for file: " + resourceToSync);
    }
    LatestResourceEtlStatusUpdater.create(getEntityManager()).updateEtlTable(resourceToSync);
    cleanUpEntityManagers();
    log.info("Added {} {} entities", addedCount.get(), resourceToSync);
  }

  private <T extends DatamartEntity> void save(T datamartEntity) {
    Object pk =
        (datamartEntity instanceof CompositeIdDatamartEntity)
            ? ((CompositeIdDatamartEntity) datamartEntity).compositeCdwId()
            : datamartEntity.cdwId();
    save(datamartEntity, pk);
  }

  private <T> void save(T entity, Object identifier) {
    EntityManager entityManager = getEntityManager();
    boolean exists = entityManager.find(entity.getClass(), identifier) != null;
    updateOrAddEntity(exists, entityManager, entity);
  }

  private <T> void updateOrAddEntity(boolean exists, EntityManager entityManager, T entity) {
    if (!exists) {
      entityManager.persist(entity);
    } else {
      entityManager.merge(entity);
    }
    addedCount.incrementAndGet();
    if ((totalRecords - addedCount.get() != 0) && (totalRecords - addedCount.get()) % 10000 == 0) {
      log.info("{} files remaining", totalRecords - addedCount.get());
    }
    entityManager.flush();
    entityManager.clear();
  }

  private class DatabaseLoader {
    File datamartDirectory;

    DatabaseLoader(File datamartDirectory) {
      this.datamartDirectory = datamartDirectory;
    }

    public <EntityT> void insertByCsvColumnNames(
        String fileName,
        Function<CSVRecord, EntityT> toEntity,
        Function<EntityT, Object> toIdentifier) {
      var csvRecords =
          CsvDataFile.builder().directory(datamartDirectory).fileName(fileName).build().records();
      totalRecords = csvRecords.size();
      log.info("Found {} records in {}", totalRecords, fileName);
      csvRecords.stream()
          .filter(Objects::nonNull)
          .distinct()
          .forEach(
              record -> {
                EntityT e = toEntity.apply(record);
                Object identifier = toIdentifier.apply(e);
                save(e, identifier);
              });
    }

    public <DM extends HasReplaceableId, E extends DatamartEntity> void insertResourceByType(
        Class<DM> resourceType, Function<DM, E> toDatamartEntity, Consumer<E> post) {
      findUniqueFiles(datamartDirectory, DatamartFilenamePatterns.get().json(resourceType))
          .parallel()
          .forEach(
              f -> {
                DM dm = MakerUtils.fileToDatamart(MAPPER, f, resourceType);
                E entity = toDatamartEntity.apply(dm);
                save(entity);
                post.accept(entity);
              });
    }

    public <DM extends HasReplaceableId, E extends DatamartEntity> void insertResourceByType(
        Class<DM> resourceType, Function<DM, E> toDatamartEntity) {
      insertResourceByType(resourceType, toDatamartEntity, (noop) -> {});
    }
  }
}
