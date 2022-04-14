package gov.va.api.health.minimartmanager.minimart.augments;

import static com.google.common.base.Preconditions.checkState;

import com.fasterxml.jackson.databind.ObjectMapper;
import gov.va.api.health.autoconfig.configuration.JacksonConfig;
import gov.va.api.health.minimartmanager.minimart.DatamartFilenamePatterns;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import lombok.SneakyThrows;

public class Minifier {
  static final ObjectMapper MAPPER = JacksonConfig.createMapper();

  public static void main(String[] args) {
    for (var path : args) {
      Path root = Path.of("../datamart", path);
      checkState(root.toFile().isDirectory(), "%s is invalid", root.toFile());
      DatamartFilenamePatterns.get().jsons().parallelStream()
          .forEach(pattern -> minify(root, pattern));
    }
  }

  @SneakyThrows
  private static void minify(Path root, String pattern) {
    var datamartResource = DatamartFilenamePatterns.get().datamartResource(pattern);
    Files.walk(root)
        .parallel()
        .map(Path::toFile)
        .filter(file -> file.isFile() && file.getName().matches(pattern))
        .forEach(file -> minify(file, datamartResource));
  }

  @SneakyThrows
  private static void minify(File file, Class<?> datamartResource) {
    MAPPER.writeValue(file, MAPPER.readValue(file, datamartResource));
  }
}
