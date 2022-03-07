package gov.va.api.health.minimartmanager.minimart;

import static java.util.stream.Collectors.toList;

import com.fasterxml.jackson.databind.ObjectMapper;
import gov.va.api.lighthouse.datamart.HasReplaceableId;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

@UtilityClass
@Slf4j
public final class MakerUtils {
  @SneakyThrows
  public <R extends HasReplaceableId> R fileToDatamart(
      ObjectMapper mapper, File f, Class<R> objectType) {
    return mapper.readValue(f, objectType);
  }

  @SneakyThrows
  public Stream<File> findUniqueFiles(File dmDirectory, String filePattern) {
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
    return uniqueFiles.stream();
  }
}
