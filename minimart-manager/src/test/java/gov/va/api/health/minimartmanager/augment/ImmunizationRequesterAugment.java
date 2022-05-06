package gov.va.api.health.minimartmanager.augment;

import gov.va.api.health.dataquery.service.controller.immunization.DatamartImmunization;
import gov.va.api.health.minimartmanager.augment.Augmentation.Context;
import java.util.Optional;

public class ImmunizationRequesterAugment {
  public static void main(String[] args) {
    Augmentation.forResources(DatamartImmunization.class)
        .whenMatching(p -> p.requester().isPresent())
        .transform(ImmunizationRequesterAugment::removeRequester)
        .build()
        .rewriteFiles();
  }

  static DatamartImmunization removeRequester(Context<DatamartImmunization> ctx) {
    ctx.resource().requester(Optional.empty());
    return ctx.resource();
  }
}
