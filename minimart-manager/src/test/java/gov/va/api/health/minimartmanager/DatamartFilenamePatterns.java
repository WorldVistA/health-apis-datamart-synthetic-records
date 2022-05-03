package gov.va.api.health.minimartmanager;

import static java.util.stream.Collectors.toList;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import gov.va.api.health.dataquery.service.controller.allergyintolerance.DatamartAllergyIntolerance;
import gov.va.api.health.dataquery.service.controller.appointment.DatamartAppointment;
import gov.va.api.health.dataquery.service.controller.condition.DatamartCondition;
import gov.va.api.health.dataquery.service.controller.device.DatamartDevice;
import gov.va.api.health.dataquery.service.controller.devicerequest.DatamartDeviceRequest;
import gov.va.api.health.dataquery.service.controller.diagnosticreport.DatamartDiagnosticReport;
import gov.va.api.health.dataquery.service.controller.encounter.DatamartEncounter;
import gov.va.api.health.dataquery.service.controller.immunization.DatamartImmunization;
import gov.va.api.health.dataquery.service.controller.location.DatamartLocation;
import gov.va.api.health.dataquery.service.controller.medication.DatamartMedication;
import gov.va.api.health.dataquery.service.controller.medicationorder.DatamartMedicationOrder;
import gov.va.api.health.dataquery.service.controller.medicationstatement.DatamartMedicationStatement;
import gov.va.api.health.dataquery.service.controller.observation.DatamartObservation;
import gov.va.api.health.dataquery.service.controller.organization.DatamartOrganization;
import gov.va.api.health.dataquery.service.controller.patient.DatamartPatient;
import gov.va.api.health.dataquery.service.controller.practitioner.DatamartPractitioner;
import gov.va.api.health.dataquery.service.controller.practitionerrole.DatamartPractitionerRole;
import gov.va.api.health.dataquery.service.controller.procedure.DatamartProcedure;
import gov.va.api.lighthouse.datamart.HasReplaceableId;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DatamartFilenamePatterns {
  private static final DatamartFilenamePatterns INSTANCE = new DatamartFilenamePatterns();

  private final BiMap<Class<?>, String> jsonFileRegex;

  private DatamartFilenamePatterns() {
    jsonFileRegex = HashBiMap.create();
    jsonFileRegex.put(DatamartAllergyIntolerance.class, "^dmAllInt.*json$");
    jsonFileRegex.put(DatamartAppointment.class, "^dmApp.*json$");
    jsonFileRegex.put(DatamartCondition.class, "^dmCon.*json$");
    jsonFileRegex.put(DatamartDevice.class, "^dmDev(?!Req).*json$");
    jsonFileRegex.put(DatamartDeviceRequest.class, "^dmDevReq.*json$");
    jsonFileRegex.put(DatamartDiagnosticReport.class, "^dmDiaRep.*json$");
    jsonFileRegex.put(DatamartEncounter.class, "^dmEnc.*json$");
    jsonFileRegex.put(DatamartImmunization.class, "^dmImm.*json$");
    jsonFileRegex.put(DatamartLocation.class, "^dmLoc.*json$");
    jsonFileRegex.put(DatamartMedication.class, "^dmMed(?!Sta|Ord).*json$");
    jsonFileRegex.put(DatamartMedicationOrder.class, "^dmMedOrd.*json$");
    jsonFileRegex.put(DatamartMedicationStatement.class, "^dmMedSta.*json$");
    jsonFileRegex.put(DatamartObservation.class, "^dmObs.*json$");
    jsonFileRegex.put(DatamartOrganization.class, "^dmOrg.*json$");
    jsonFileRegex.put(DatamartPatient.class, "^dmPat.*json$");
    jsonFileRegex.put(DatamartPractitioner.class, "^dmPra[^R].*json$");
    jsonFileRegex.put(DatamartPractitionerRole.class, "^dmPraRol.*json$");
    jsonFileRegex.put(DatamartProcedure.class, "^dmPro.*json$");
  }

  @SneakyThrows
  public static <R extends HasReplaceableId> R fileToDatamart(
      ObjectMapper mapper, File f, Class<R> objectType) {
    return mapper.readValue(f, objectType);
  }

  @SneakyThrows
  public static List<File> findUniqueFiles(File dmDirectory, String filePattern) {
    List<File> files =
        Files.walk(dmDirectory.toPath())
            .map(Path::toFile)
            .filter(File::isFile)
            .filter(f -> f.getName().matches(filePattern))
            .collect(toList());
    Set<String> fileNames = new HashSet<>();
    List<File> uniqueFiles = new ArrayList<>();
    for (File file : files) {
      if (fileNames.add(file.getName())) {
        uniqueFiles.add(file);
      }
    }
    log.info("{} unique files found for {}", uniqueFiles.size(), filePattern);
    Collections.sort(uniqueFiles);
    return uniqueFiles;
  }

  public static DatamartFilenamePatterns get() {
    return INSTANCE;
  }

  public Class<?> datamartResource(String json) {
    return getOrDie(
        jsonFileRegex.inverse(), json, String.format("Datamart resource not found for: {}", json));
  }

  private <K, V> V getOrDie(Map<K, V> map, K key, String message) {
    var value = map.get(key);
    if (value == null) {
      throw new IllegalArgumentException(message);
    }
    return value;
  }

  public String json(Class<?> datamartResource) {
    return getOrDie(
        jsonFileRegex,
        datamartResource,
        String.format("File pattern not found for: {}", datamartResource.getName()));
  }

  public Collection<String> jsons() {
    return jsonFileRegex.values();
  }
}
