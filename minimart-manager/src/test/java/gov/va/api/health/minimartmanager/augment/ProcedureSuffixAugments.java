package gov.va.api.health.minimartmanager.augment;

import static com.google.common.base.Preconditions.checkNotNull;

import gov.va.api.health.dataquery.service.controller.procedure.DatamartProcedure;
import gov.va.api.lighthouse.datamart.CompositeCdwId;
import java.util.Objects;

public class ProcedureSuffixAugments {
  static DatamartProcedure addIdSuffix(Augmentation.Context<DatamartProcedure> ctx) {
    var dm = ctx.resource();
    if (!dm.cdwId().endsWith(":S")) {
      dm.cdwId(dm.cdwId() + ":S");
    }
    checkNotNull(CompositeCdwId.fromCdwId(dm.cdwId()));
    return dm;
  }

  public static void main(String[] args) {
    Augmentation.forResources(DatamartProcedure.class)
        .whenMatching(Objects::nonNull)
        .transform(ProcedureSuffixAugments::addIdSuffix)
        .build()
        .rewriteFiles();
  }
}
