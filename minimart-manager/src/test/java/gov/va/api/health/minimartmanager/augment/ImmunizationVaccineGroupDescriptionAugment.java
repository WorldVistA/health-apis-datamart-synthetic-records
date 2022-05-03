package gov.va.api.health.minimartmanager.augment;

import static com.google.common.base.Preconditions.checkState;

import com.google.common.collect.Iterables;
import gov.va.api.health.dataquery.service.controller.immunization.DatamartImmunization;
import java.util.Objects;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;

public class ImmunizationVaccineGroupDescriptionAugment {

  public static void main(String[] args) {
    Augmentation.forResources(DatamartImmunization.class)
        .whenMatching(Objects::nonNull)
        .transform(ImmunizationVaccineGroupDescriptionAugment::updateShortDescription)
        .build()
        .rewriteFiles();
  }

  static DatamartImmunization updateShortDescription(
      Augmentation.Context<DatamartImmunization> ctx) {
    DatamartImmunization dm = ctx.resource();

    var coding = dm.vaccineCode().coding();
    checkState(!coding.isEmpty());

    if (coding.size() == 1) {
      return dm;
    }

    var lastDisplay = Iterables.getLast(coding).display().get();

    checkState(StringUtils.isNotBlank(lastDisplay));

    lastDisplay = "VACCINE GROUP: " + lastDisplay;

    Iterables.getLast(coding).display(Optional.of(lastDisplay));
    return dm;
  }
}
