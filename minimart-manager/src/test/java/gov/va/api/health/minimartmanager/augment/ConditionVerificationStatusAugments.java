package gov.va.api.health.minimartmanager.augment;

import static com.google.common.base.Preconditions.checkState;

import gov.va.api.health.dataquery.service.controller.condition.DatamartCondition;
import java.util.Objects;

public class ConditionVerificationStatusAugments {
  public static void main(String[] args) {
    Augmentation.forResources(DatamartCondition.class)
        .whenMatching(Objects::nonNull)
        .transform(ConditionVerificationStatusAugments::updateStatus)
        .build()
        .rewriteFiles();
  }

  static DatamartCondition updateStatus(Augmentation.Context<DatamartCondition> ctx) {
    ctx.resource().verificationStatus(verificationStatus(ctx));
    return ctx.resource();
  }

  private static DatamartCondition.VerificationStatus verificationStatus(
      Augmentation.Context<DatamartCondition> ctx) {
    checkState(ctx.resource().clinicalStatus() != null);
    double r = ctx.random().nextDouble();
    if (ctx.resource().clinicalStatus() == DatamartCondition.ClinicalStatus.active) {
      // 50% confirmed, 20% null, 15% provisional, 15% refuted
      if (r < 0.50) {
        return DatamartCondition.VerificationStatus.confirmed;
      } else if (r < 0.70) {
        return null;
      } else if (r < 0.85) {
        return DatamartCondition.VerificationStatus.provisional;
      } else {
        return DatamartCondition.VerificationStatus.refuted;
      }
    }

    // 80% confirmed, 8% provisional, 7% refuted, 5% null
    if (r < 0.80) {
      return DatamartCondition.VerificationStatus.confirmed;
    } else if (r < 0.88) {
      return DatamartCondition.VerificationStatus.provisional;
    } else if (r < 0.95) {
      return DatamartCondition.VerificationStatus.refuted;
    } else {
      return null;
    }
  }
}
