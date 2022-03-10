package gov.va.api.health.minimartmanager;

import static com.google.common.base.Preconditions.checkState;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.apache.commons.lang3.StringUtils.trimToNull;

import com.fasterxml.jackson.databind.ObjectMapper;
import gov.va.api.health.autoconfig.configuration.JacksonConfig;
import gov.va.api.health.dataquery.service.controller.allergyintolerance.DatamartAllergyIntolerance;
import gov.va.api.health.dataquery.service.controller.appointment.DatamartAppointment;
import gov.va.api.health.dataquery.service.controller.condition.DatamartCondition;
import gov.va.api.health.dataquery.service.controller.diagnosticreport.DatamartDiagnosticReport;
import gov.va.api.health.dataquery.service.controller.patient.DatamartPatient;
import gov.va.api.health.minimartmanager.minimart.DatamartFilenamePatterns;
import gov.va.api.health.minimartmanager.minimart.MakerUtils;
import gov.va.api.lighthouse.datamart.DatamartCoding;
import gov.va.api.lighthouse.datamart.DatamartReference;
import gov.va.api.lighthouse.datamart.HasReplaceableId;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import lombok.Builder;
import lombok.SneakyThrows;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.junit.jupiter.api.Test;

@Slf4j
public class GenerateCsv {
  /** Resources to generate csv entries. To be filled out over time */
  private static final List<String> ALL_RESOURCES =
      List.of("AllergyIntolerance", "Appointment", "Condition", "DiagnosticReport");

  private static final String[] CSV_HEADERS = {
    "PatientEmail",
    "PatientIcn",
    "Name",
    "Birthdate",
    "Resource",
    "CodeSystem",
    "Code",
    "Description",
    "Status",
    "Classification",
    "Date"
  };

  private static final String CSV_NAME = "health-test-patient-data.csv";

  private static final ObjectMapper MAPPER = JacksonConfig.createMapper();

  private static final Map<String, String> ICN_TO_EMAIL = loadEmails();

  private static final Map<String, DatamartPatient> ICN_TO_PATIENT = loadPatients();

  private final Function<DatamartAllergyIntolerance, CsvModel> toAllergyIntoleranceCsv =
      dmAllergyIntolerance -> {
        var icn = dmAllergyIntolerance.patient().reference().get();
        if (!ICN_TO_EMAIL.containsKey(icn)) {
          return null;
        }
        var dmPatient = getOrThrow("patient", ICN_TO_PATIENT, icn);
        return CsvModel.forPatient(dmPatient)
            .resource("AllergyIntolerance")
            .codeSystem(
                dmAllergyIntolerance
                    .substance()
                    .flatMap(DatamartAllergyIntolerance.Substance::coding)
                    .flatMap(DatamartCoding::system)
                    .orElse(""))
            .code(
                dmAllergyIntolerance
                    .substance()
                    .flatMap(DatamartAllergyIntolerance.Substance::coding)
                    .flatMap(DatamartCoding::code)
                    .orElse(""))
            .description(
                dmAllergyIntolerance
                    .substance()
                    .flatMap(DatamartAllergyIntolerance.Substance::coding)
                    .flatMap(DatamartCoding::display)
                    .orElse(""))
            .status(dmAllergyIntolerance.clinicalStatus().name())
            .classification("")
            .date(
                Objects.requireNonNull(dmAllergyIntolerance.recordedDate().orElse(null)).toString())
            .build();
      };

  private final Function<DatamartAppointment, CsvModel> toAppointmentCsv =
      dmAppointment -> {
        var icn =
            dmAppointment.participant().stream()
                .filter(p -> "PATIENT".equalsIgnoreCase(p.type().orElse(null)))
                .findFirst()
                .flatMap(DatamartReference::reference)
                .get();
        if (!ICN_TO_EMAIL.containsKey(icn)) {
          return null;
        }
        var dmPatient = getOrThrow("patient", ICN_TO_PATIENT, icn);
        return CsvModel.forPatient(dmPatient)
            .resource("Appointment")
            .codeSystem("")
            .code("")
            .description(dmAppointment.description().orElse(""))
            .status(dmAppointment.status().orElse(""))
            .classification(dmAppointment.serviceType())
            .date(Objects.requireNonNull(dmAppointment.start().orElse(null)).toString())
            .build();
      };

  private final Function<DatamartCondition, CsvModel> toConditionCsv =
      dmCondition -> {
        var icn = dmCondition.patient().reference().get();
        if (!ICN_TO_EMAIL.containsKey(icn)) {
          return null;
        }
        var dmPatient = getOrThrow("patient", ICN_TO_PATIENT, icn);
        String codeSystem = "";
        String code = "";
        String description = "";
        if (dmCondition.hasSnomedCode()) {
          var snomed = dmCondition.snomed().get();
          codeSystem = "SNOMED";
          code = snomed.code();
          description = snomed.display();
        } else if (dmCondition.hasIcdCode()) {
          var icd = dmCondition.icd().get();
          codeSystem = "ICD";
          code = icd.code();
          description = icd.display();
        }
        return CsvModel.forPatient(dmPatient)
            .resource("Condition")
            .codeSystem(codeSystem)
            .code(code)
            .description(description)
            .status(dmCondition.clinicalStatus().name())
            .classification(dmCondition.category().name())
            .date(Objects.requireNonNull(dmCondition.onsetDateTime().orElse(null)).toString())
            .build();
      };

  private final Function<DatamartDiagnosticReport, CsvModel> toDiagnosticReportCsv =
      dmDiagnosticReport -> {
        var icn = dmDiagnosticReport.patient().reference().get();
        if (!ICN_TO_EMAIL.containsKey(icn)) {
          return null;
        }
        var dmPatient = getOrThrow("patient", ICN_TO_PATIENT, icn);
        return CsvModel.forPatient(dmPatient)
            .resource("DiagnosticReport")
            .codeSystem("")
            .code("")
            .description("panel")
            .status("final")
            .classification("Laboratory")
            .date(Objects.requireNonNull(dmDiagnosticReport.effectiveDateTime()))
            .build();
      };

  static <T> T getOrThrow(String description, Map<String, T> map, String key) {
    var value = map.get(key);
    checkState(value != null, "Missing %s for %s", description, key);
    return value;
  }

  static File importDirectory() {
    var importDirectoryPath = System.getProperty("import.directory");
    if (isBlank(importDirectoryPath)) {
      throw new IllegalStateException("import.directory not specified");
    }
    return new File(importDirectoryPath);
  }

  @SneakyThrows
  static Map<String, String> loadEmails() {
    // see health_test_accounts.md in vets-api-clients
    try (var reader =
        new InputStreamReader(
            GenerateCsv.class.getResourceAsStream("test-patient-emails.csv"),
            StandardCharsets.UTF_8)) {
      var rows = CSVFormat.DEFAULT.parse(reader);
      var map = new HashMap<String, String>();
      for (var row : rows) {
        checkState(row.size() == 2);
        var email = trimToNull(row.get(0));
        checkState(email != null, "row %s missing email", row);
        var icn = trimToNull(row.get(1));
        checkState(icn != null, "row %s missing icn", row);
        checkState(!map.containsKey(icn), "row %s duplicate icn", row);
        map.put(icn, email);
      }
      return Collections.unmodifiableMap(map);
    }
  }

  static Map<String, DatamartPatient> loadPatients() {
    var map = new HashMap<String, DatamartPatient>();
    MakerUtils.findUniqueFiles(
            importDirectory(), DatamartFilenamePatterns.get().json(DatamartPatient.class))
        .stream()
        .map(f -> MakerUtils.fileToDatamart(MAPPER, f, DatamartPatient.class))
        .forEach(dmPatient -> map.put(dmPatient.fullIcn(), dmPatient));
    return Collections.unmodifiableMap(map);
  }

  @Test
  @SneakyThrows
  void createCsv() {
    try (var printer =
        new CSVPrinter(new FileWriter(CSV_NAME), CSVFormat.DEFAULT.withHeader(CSV_HEADERS))) {
      // per resource, add the datamart records found in the import directory to the csv file.
      var resourcesToUpdate = ALL_RESOURCES;
      var userSpecifiedResources = System.getProperty("resources");
      if (isNotBlank(userSpecifiedResources)) {
        resourcesToUpdate = Arrays.asList(userSpecifiedResources.split(",", -1));
        log.warn("Overriding default resources. Only summarizing {}", resourcesToUpdate);
      }
      var importDirectory = importDirectory();
      for (var resource : resourcesToUpdate) {
        log.info(
            "Adding to csv file with RESOURCE: {}, IMPORT DIRECTORY: {}",
            resource,
            importDirectory);
        for (var record : toCsvRecords(importDirectory, resource)) {
          printer.printRecord(record.csvRow());
        }
      }
    }
  }

  List<CsvModel> toCsvRecords(File dir, String resource) {
    switch (resource) {
      case "AllergyIntolerance":
        return toCsvRecords(dir, DatamartAllergyIntolerance.class, toAllergyIntoleranceCsv);
      case "Appointment":
        return toCsvRecords(dir, DatamartAppointment.class, toAppointmentCsv);
      case "Condition":
        return toCsvRecords(dir, DatamartCondition.class, toConditionCsv);
      case "DiagnosticReport":
        return toCsvRecords(dir, DatamartDiagnosticReport.class, toDiagnosticReportCsv);
      default:
        throw new RuntimeException("Unsupported resource type: " + resource);
    }
  }

  <DM extends HasReplaceableId> List<CsvModel> toCsvRecords(
      File directory, Class<DM> resourceType, Function<DM, CsvModel> toDatamartCsv) {
    return MakerUtils.findUniqueFiles(directory, DatamartFilenamePatterns.get().json(resourceType))
        .parallelStream()
        .map(f -> toDatamartCsv.apply(MakerUtils.fileToDatamart(MAPPER, f, resourceType)))
        .filter(Objects::nonNull)
        .collect(toList());
  }

  @Value
  @Builder
  private static class CsvModel {
    String patientEmail;

    String patientIcn;

    String name;

    String birthdate;

    String resource;

    String codeSystem;

    String code;

    String description;

    String status;

    String classification;

    String date;

    // Use the patient map to find the patient's name and birthdate by ICN
    static CsvModelBuilder forPatient(DatamartPatient dmPatient) {
      return CsvModel.builder()
          .patientEmail(getOrThrow("email", ICN_TO_EMAIL, dmPatient.fullIcn()))
          .patientIcn(dmPatient.fullIcn())
          .name(dmPatient.name())
          .birthdate(dmPatient.birthDateTime().substring(0, 10));
    }

    public List<String> csvRow() {
      return List.of(
          patientEmail,
          patientIcn,
          name,
          birthdate,
          resource,
          codeSystem,
          code,
          description,
          status,
          classification,
          date);
    }
  }
}
