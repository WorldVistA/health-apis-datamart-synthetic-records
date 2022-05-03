package gov.va.api.health.minimartmanager.augment;

import static java.util.stream.Collectors.toList;

import gov.va.api.health.dataquery.service.controller.immunization.DatamartImmunization;
import gov.va.api.health.fhir.api.Safe;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class ImmunizationDoseQuantityAugments {
  private static final List<String> COVID_VACCINE_CODES =
      List.of(
          "207", "208", "210", "211", "212", "213", "217", "218", "219", "500", "501", "502", "503",
          "504", "505", "506", "507", "508", "509", "510", "511");

  private static final List<Optional<DatamartImmunization.Quantity>> QUANTITIES =
      List.of(
          Optional.empty(),
          quantity(0.50, "mL"),
          quantity(2.00, "ug/mL"),
          quantity(0.30, "mL"),
          quantity(2.00, null));

  static DatamartImmunization addQuantity(Augmentation.Context<DatamartImmunization> ctx) {
    var quantity = ctx.random(QUANTITIES);
    var codes =
        Safe.stream(
                Optional.ofNullable(ctx.resource().vaccineCode())
                    .map(vc -> vc.coding())
                    .orElse(null))
            .map(c -> c.code().get())
            .collect(toList());
    if (COVID_VACCINE_CODES.stream().anyMatch(codes::contains)) {
      while (quantity.isEmpty()) {
        quantity = ctx.random(QUANTITIES);
      }
    }
    ctx.resource().doseQuantity(quantity);
    return ctx.resource();
  }

  public static void main(String[] args) {
    Augmentation.forResources(DatamartImmunization.class)
        .whenMatching(Objects::nonNull)
        .transform(ImmunizationDoseQuantityAugments::addQuantity)
        .build()
        .rewriteFiles();
  }

  private static Optional<DatamartImmunization.Quantity> quantity(double dosage, String doseUnits) {
    return Optional.of(
        DatamartImmunization.Quantity.builder()
            .system(Optional.of("http://unitsofmeasure.org"))
            .code(Optional.ofNullable(doseUnits))
            .value(Optional.of(dosage))
            .unit(Optional.ofNullable(doseUnits))
            .build());
  }
}
