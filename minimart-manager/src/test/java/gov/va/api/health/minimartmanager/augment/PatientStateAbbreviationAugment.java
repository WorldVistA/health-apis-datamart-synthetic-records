package gov.va.api.health.minimartmanager.augment;

import static com.google.common.base.Preconditions.checkState;

import gov.va.api.health.dataquery.service.controller.patient.DatamartPatient;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import lombok.SneakyThrows;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

public class PatientStateAbbreviationAugment {
  private static final Map<String, String> STATE_ABBREVIATIONS = initStateAbbreviations();

  static DatamartPatient addStateAbbreviations(Augmentation.Context<DatamartPatient> ctx) {
    DatamartPatient dm = ctx.resource();
    var addresses = dm.address();
    checkState(!addresses.isEmpty());
    for (var address : addresses) {
      updateAddress(address);
    }
    var contacts = dm.contact();
    for (var contact : contacts) {
      updateAddress(contact.address());
    }
    return dm;
  }

  @SneakyThrows
  static TreeMap<String, String> initStateAbbreviations() {
    var csvFile =
        PatientStateAbbreviationAugment.class.getResourceAsStream(
            "cdw-state-state-abbreviations.csv");
    InputStreamReader reader = new InputStreamReader(csvFile);
    Iterable<CSVRecord> rows = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(reader);
    TreeMap<String, String> csvStateMap = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    for (CSVRecord row : rows) {
      checkState(row.size() == 2);
      csvStateMap.put(row.get(0), row.get(1));
    }
    return csvStateMap;
  }

  public static void main(String[] args) {
    Augmentation.forResources(DatamartPatient.class)
        .whenMatching(Objects::nonNull)
        .transform(PatientStateAbbreviationAugment::addStateAbbreviations)
        .build()
        .rewriteFiles();
  }

  static void updateAddress(DatamartPatient.Address address) {
    if (address.state() != null) {
      var abbreviation = STATE_ABBREVIATIONS.get(address.state());
      checkState(abbreviation != null);
      address.stateAbbreviation(abbreviation);
    }
  }
}
