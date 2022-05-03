package gov.va.api.health.minimartmanager.augment;

import static org.apache.commons.lang3.StringUtils.isBlank;

import com.google.common.collect.ImmutableMap;
import gov.va.api.health.dataquery.service.controller.allergyintolerance.DatamartAllergyIntolerance;
import gov.va.api.lighthouse.datamart.DatamartCoding;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class AllergyIntoleranceSubstanceAugments {
  private static final Map<String, DatamartCoding> SUBSTANCE_TEXT_TO_CODING_MAPPINGS =
      new ImmutableMap.Builder<String, DatamartCoding>()
          .put(
              "House dust mite allergy",
              datamartCodingFromParts(
                  "232350006", "House dust mite allergy", "http://snomed.info/sct"))
          .put(
              "Dander (animal) allergy",
              datamartCodingFromParts(
                  "232347008", "Dander (animal) allergy", "http://snomed.info/sct"))
          .put(
              "Allergy to grass pollen",
              datamartCodingFromParts(
                  "418689008", "Allergy to grass pollen", "http://snomed.info/sct"))
          .put(
              "Allergy to tree pollen",
              datamartCodingFromParts(
                  "782576004", "Allergy to tree pollen", "http://snomed.info/sct"))
          .put(
              "Shellfish allergy",
              datamartCodingFromParts("300913006", "Shellfish allergy", "http://snomed.info/sct"))
          .put(
              "Allergy to fish",
              datamartCodingFromParts("417532002", "Allergy to fish", "http://snomed.info/sct"))
          .put(
              "Allergy to peanuts",
              datamartCodingFromParts("91935009", "Allergy to peanuts", "http://snomed.info/sct"))
          .put(
              "Allergy to bee venom",
              datamartCodingFromParts(
                  "424213003", "Allergy to bee venom", "http://snomed.info/sct"))
          .put(
              "Allergy to mould",
              datamartCodingFromParts("419474003", "Allergy to mould", "http://snomed.info/sct"))
          .put(
              "Allergy to dairy product",
              datamartCodingFromParts(
                  "425525006", "Allergy to dairy product", "http://snomed.info/sct"))
          .put(
              "Allergy to wheat",
              datamartCodingFromParts("420174000", "Allergy to wheat", "http://snomed.info/sct"))
          .put(
              "Latex allergy",
              datamartCodingFromParts("300916003", "Latex allergy", "http://snomed.info/sct"))
          .put(
              "Allergy to eggs",
              datamartCodingFromParts("91930004", "Allergy to eggs", "http://snomed.info/sct"))
          .put(
              "Allergy to nut",
              datamartCodingFromParts("91934008", "Allergy to nut", "http://snomed.info/sct"))
          .put(
              "Allergy to soya",
              datamartCodingFromParts("197493001", "Allergy to soya", "http://snomed.info/sct"))
          .build();

  private static DatamartAllergyIntolerance addCodingToSubstance(
      DatamartAllergyIntolerance allergyIntolerance) {
    return allergyIntolerance.substance(
        Optional.of(
            DatamartAllergyIntolerance.Substance.builder()
                .text(allergyIntolerance.substance().get().text())
                .coding(
                    Optional.of(
                        SUBSTANCE_TEXT_TO_CODING_MAPPINGS.get(
                            allergyIntolerance.substance().get().text())))
                .build()));
  }

  static DatamartAllergyIntolerance code(Augmentation.Context<DatamartAllergyIntolerance> ctx) {
    if (!ctx.resource().substance().isPresent()
        || isBlank(ctx.resource().substance().get().text())) {
      System.out.println(
          "No substance text exists for allergyIntolerance.cdwId: " + ctx.resource().cdwId());
      return ctx.resource();
    }
    if (SUBSTANCE_TEXT_TO_CODING_MAPPINGS.containsKey(ctx.resource().substance().get().text())) {
      return addCodingToSubstance(ctx.resource());
    }
    return ctx.resource();
  }

  private static DatamartCoding datamartCodingFromParts(
      String code, String display, String system) {
    return DatamartCoding.builder()
        .code(Optional.of(code))
        .display(Optional.of(display))
        .system(Optional.of(system))
        .build();
  }

  public static void main(String[] args) {
    Augmentation.forResources(DatamartAllergyIntolerance.class)
        .whenMatching(Objects::nonNull)
        .transform(AllergyIntoleranceSubstanceAugments::code)
        .build()
        .rewriteFiles();
  }
}
