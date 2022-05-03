package gov.va.api.health.minimartmanager.augment;

import gov.va.api.health.dataquery.service.controller.practitioner.DatamartPractitioner;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class PractitionerQualificationTextAndPeriodAugment {
  private static final List<Optional<DatamartPractitioner.Period>> PERIODS =
      List.of(period(null, null), period("2010-01-01", null), period("2010-01-01", "2015-04-02"));

  public static void main(String[] args) {
    Augmentation.forResources(DatamartPractitioner.class)
        .whenMatching(Objects::nonNull)
        .transform(PractitionerQualificationTextAndPeriodAugment::qualificationTextAndPeriod)
        .build()
        .rewriteFiles();
  }

  private static Optional<DatamartPractitioner.Period> period(String start, String end) {
    Optional<LocalDate> maybeStart = start != null ? Optional.of(LocalDate.parse(start)) : null;
    Optional<LocalDate> maybeEnd = end != null ? Optional.of(LocalDate.parse(end)) : null;

    return Optional.of(
        DatamartPractitioner.Period.builder().start(maybeStart).end(maybeEnd).build());
  }

  static DatamartPractitioner qualificationTextAndPeriod(
      Augmentation.Context<DatamartPractitioner> ctx) {
    if (ctx.resource().address().isEmpty()) {
      System.out.println("No addresses exist for practitioner.cdwId: " + ctx.resource().cdwId());
      return ctx.resource();
    }

    if (ctx.resource().name().prefix().isPresent() && ctx.resource().name().suffix().isPresent()) {
      String prefix = ctx.resource().name().prefix().get();
      String suffix = ctx.resource().name().suffix().get();

      if (suffix.equals("MD")) {
        ctx.resource().qualificationText(Optional.of(suffix));
      } else {
        ctx.resource().qualificationText(Optional.of(prefix));
      }
    }

    for (DatamartPractitioner.Address address : ctx.resource().address()) {
      var maybePeriod = ctx.random(PERIODS);
      if (maybePeriod.get().start().isPresent() || maybePeriod.get().end().isPresent()) {
        address.period(maybePeriod);
      }
    }

    return ctx.resource();
  }
}
