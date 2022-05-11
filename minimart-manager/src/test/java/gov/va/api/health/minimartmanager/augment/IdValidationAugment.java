package gov.va.api.health.minimartmanager.augment;

import static com.google.common.base.Preconditions.checkState;

import com.fasterxml.jackson.databind.ObjectMapper;
import gov.va.api.health.autoconfig.configuration.JacksonConfig;
import gov.va.api.health.dataquery.service.controller.medicationorder.DatamartMedicationOrder;
import gov.va.api.health.dataquery.service.controller.patient.DatamartPatient;
import gov.va.api.health.minimartmanager.DatamartFilenamePatterns;
import gov.va.api.lighthouse.datamart.HasReplaceableId;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.regex.Pattern;
import lombok.SneakyThrows;

public class IdValidationAugment {
  static final ObjectMapper MAPPER = JacksonConfig.createMapper();

  static final Pattern FILE_ICN_PATTERN = Pattern.compile(".*dm-records-(\\w+).*");

  @SneakyThrows
  private static void checkDuplicates() {
    var list = new ArrayList<>(1_000_000);
    Files.walk(Path.of("../datamart"))
        .parallel()
        .filter(p -> p.toFile().isFile())
        .map(p -> p.toFile().getName())
        .filter(n -> n.endsWith(".json"))
        .sorted()
        .forEachOrdered(n -> list.add(n));
    var set = new HashSet<>(1_000_000);
    var duplicates = new LinkedHashSet<>();
    for (var e : list) {
      var isDuplicate = !set.add(e);
      if (isDuplicate) {
        duplicates.add(e);
      }
    }
    checkState(duplicates.isEmpty(), "Duplicates: %s", duplicates);
  }

  @SneakyThrows
  private static void checkIdAndRewrite(Path root, String pattern) {
    var datamartResource = DatamartFilenamePatterns.get().datamartResource(pattern);
    Files.walk(root)
        .parallel()
        .map(Path::toFile)
        .filter(file -> file.isFile() && file.getName().matches(pattern))
        .forEach(file -> checkIdAndRewrite(file, datamartResource));
  }

  @SneakyThrows
  private static void checkIdAndRewrite(File file, Class<?> datamartResource) {
    var payload = MAPPER.readValue(file, datamartResource);
    var payloadId =
        file.getName().startsWith("dmPat")
            ? ((DatamartPatient) payload).fullIcn()
            : ((HasReplaceableId) payload).cdwId();
    var fileId = payloadId.replace(":", "").toUpperCase(Locale.US);
    checkState(file.getName().endsWith(fileId + ".json"), "ID mismatch for %s", file);
    // TODO support all resources
    if (payload instanceof DatamartMedicationOrder mo) {
      var matcher = FILE_ICN_PATTERN.matcher(file.getPath());
      var found = matcher.find();
      checkState(found && matcher.groupCount() == 1, "no ICN in path of %s", file);
      var fileIcn = matcher.group(1);
      var payloadIcn = mo.patient().reference().get();
      checkState(fileIcn.equals(payloadIcn), "ICN mismatch for %s", file);
    }
    MAPPER.writeValue(file, payload);
  }

  public static void main(String[] args) {
    for (var path : args) {
      Path root = Path.of("../datamart", path);
      checkState(root.toFile().isDirectory(), "%s is invalid", root.toFile());
      DatamartFilenamePatterns.get().jsons().parallelStream()
          .forEach(pattern -> checkIdAndRewrite(root, pattern));
    }
    checkDuplicates();
  }
}
