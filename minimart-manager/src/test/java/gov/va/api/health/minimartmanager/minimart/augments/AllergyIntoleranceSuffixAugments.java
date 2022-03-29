package gov.va.api.health.minimartmanager.minimart.augments;

import static com.google.common.base.Preconditions.checkNotNull;

import gov.va.api.health.dataquery.service.controller.allergyintolerance.DatamartAllergyIntolerance;
import gov.va.api.lighthouse.datamart.CompositeCdwId;
import java.util.Objects;

public class AllergyIntoleranceSuffixAugments {
  static DatamartAllergyIntolerance addIdSuffix(
      Augmentation.Context<DatamartAllergyIntolerance> ctx) {
    var dm = ctx.resource();
    if (!dm.cdwId().endsWith(":A")) {
      dm.cdwId(dm.cdwId() + ":A");
    }
    checkNotNull(CompositeCdwId.fromCdwId(dm.cdwId()));
    return dm;
  }

  public static void main(String[] args) {
    Augmentation.forResources(DatamartAllergyIntolerance.class)
        .whenMatching(Objects::nonNull)
        .transform(AllergyIntoleranceSuffixAugments::addIdSuffix)
        .build()
        .rewriteFiles();
  }
}
