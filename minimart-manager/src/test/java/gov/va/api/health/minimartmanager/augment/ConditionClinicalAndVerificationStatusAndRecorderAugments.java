package gov.va.api.health.minimartmanager.augment;

import static com.google.common.base.Preconditions.checkState;

import gov.va.api.health.dataquery.service.controller.condition.DatamartCondition;
import gov.va.api.health.minimartmanager.augment.Augmentation.Context;
import java.util.Objects;

public class ConditionClinicalAndVerificationStatusAndRecorderAugments {
  public static void main(String[] args) {
    Augmentation.forResources(DatamartCondition.class)
        .whenMatching(Objects::nonNull)
        .transform(
            ConditionClinicalAndVerificationStatusAndRecorderAugments
                ::updateClinicalStatusAndAddVerificationStatusAndRecorder)
        .build()
        .rewriteFiles();
  }

  static DatamartCondition updateClinicalStatusAndAddVerificationStatusAndRecorder(
      Augmentation.Context<DatamartCondition> ctx) {
    checkState(ctx.resource().verificationStatus() == null);
    checkState(ctx.resource().recorder().isEmpty());
    updateClinicalStatusCode(ctx);
    ctx.resource().verificationStatus(DatamartCondition.VerificationStatus.unconfirmed);
    ctx.resource().recorder(ctx.resource().asserter());
    return ctx.resource();
  }

  static DatamartCondition updateClinicalStatusCode(Context<DatamartCondition> ctx) {
    if (ctx.random().nextBoolean()
        && ctx.resource().clinicalStatus() == DatamartCondition.ClinicalStatus.resolved) {
      ctx.resource().clinicalStatus(DatamartCondition.ClinicalStatus.inactive);
    }
    return ctx.resource();
  }
}
