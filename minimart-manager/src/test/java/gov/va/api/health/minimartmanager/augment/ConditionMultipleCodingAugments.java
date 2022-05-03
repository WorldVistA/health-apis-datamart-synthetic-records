package gov.va.api.health.minimartmanager.augment;

import com.google.common.collect.ImmutableMap;
import gov.va.api.health.dataquery.service.controller.condition.DatamartCondition;
import gov.va.api.health.dataquery.service.controller.condition.DatamartCondition.SnomedCode;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import org.springframework.data.util.Pair;

public class ConditionMultipleCodingAugments {
  private static final Map<String, Pair<String, String>> ICD_TO_SNOMED_MAPPINGS =
      new ImmutableMap.Builder<String, Pair<String, String>>()
          .put("C18.8", Pair.of("109838007", "Overlapping malignant neoplasm of colon"))
          .put("J45.51", Pair.of("195967001", "Asthma"))
          .put("R19.7", Pair.of("236077008", "Protracted diarrhea"))
          .put("R82.79", Pair.of("301011002", "Escherichia coli urinary tract infection"))
          .put("C26.0", Pair.of("363406005", "Malignant tumor of colon"))
          .put("A56.01", Pair.of("38822007", "Cystitis"))
          .put("S83.511D", Pair.of("444470001", "Injury of anterior cruciate ligament"))
          .put("K57.31", Pair.of("6072007", "Bleeding from anus"))
          .put("M71.161", Pair.of("90560007", "Gout"))
          .put(
              "E11.37X3",
              Pair.of(
                  "97331000119101",
                  "Macular edema and retinopathy due to type 2 diabetes mellitus (disorder)"))
          .put("M87.051", Pair.of("239872002", "Osteoarthritis of hip"))
          .put("S91.312A", Pair.of("284551006", "Laceration of foot"))
          .put("S83.419A", Pair.of("444448004", "Injury of medial collateral ligament of knee"))
          .put(
              "E11.329",
              Pair.of(
                  "1501000119109",
                  "Proliferative diabetic retinopathy due to type II diabetes mellitus (disorder)"))
          .put("L20.81", Pair.of("24079001", "Atopic dermatitis"))
          .put("S62.90XD", Pair.of("263102004", "Fracture subluxation of wrist"))
          .put("M84.459A", Pair.of("359817006", "Closed fracture of hip"))
          .put("S01.119A", Pair.of("370247008", "Facial laceration"))
          .put("G40.409", Pair.of("84757009", "Epilepsy"))
          .put("J34.9", Pair.of("232353008", "Perennial allergic rhinitis with seasonal variation"))
          .put("S06.300D", Pair.of("62106007", "Concussion with no loss of consciousness"))
          .put("M80.871A", Pair.of("16114001", "Fracture of ankle"))
          .put(
              "C61.",
              Pair.of("314994000", "Metastasis from malignant tumor of prostate (disorder)"))
          .put("S52.91XD", Pair.of("65966004", "Fracture of forearm"))
          .put("J98.01", Pair.of("75498004", "Acute bacterial sinusitis (disorder)"))
          .put("G44.221", Pair.of("124171000119105", "Chronic intractable migraine without aura"))
          .put("K01.1", Pair.of("196416002", "Impacted molars"))
          .put("M18.11", Pair.of("239873007", "Osteoarthritis of knee"))
          .put("Z86.74", Pair.of("399211009", "History of myocardial infarction (situation)"))
          .put("K35.2", Pair.of("428251008", "History of appendectomy"))
          .put("G40.309", Pair.of("703151001", "History of single seizure (situation)"))
          .put("J44.9", Pair.of("410429000", "Cardiac Arrest"))
          .put(
              "M80.862G",
              Pair.of("443165006", "Pathological fracture due to osteoporosis (disorder)"))
          .put("S93.412S", Pair.of("44465007", "Sprain of ankle"))
          .put(
              "E11.22",
              Pair.of("157141000119108", "Proteinuria due to type 2 diabetes mellitus (disorder)"))
          .put("Z86.51", Pair.of("47505003", "Posttraumatic stress disorder"))
          .put("C34.01", Pair.of("254632001", "Small cell carcinoma of lung (disorder)"))
          .put("J20.8", Pair.of("36971009", "Sinusitis (disorder)"))
          .put("M80.071S", Pair.of("64859006", "Osteoporosis (disorder)"))
          .put("K63.5", Pair.of("68496003", "Polyp of colon"))
          .put(
              "E08.319",
              Pair.of(
                  "422034002",
                  "Diabetic retinopathy associated with type II diabetes mellitus (disorder)"))
          .put("H66.3X3", Pair.of("65363002", "Otitis media"))
          .put("F17.219", Pair.of("449868002", "Smokes tobacco daily"))
          .put("G30.9", Pair.of("26929004", "Alzheimer's disease (disorder)"))
          .put("J96.11", Pair.of("49436004", "Atrial Fibrillation"))
          .put("J01.21", Pair.of("40055000", "Chronic sinusitis (disorder)"))
          .put("E10.8", Pair.of("55822004", "Hyperlipidemia"))
          .put("Z68.38", Pair.of("162864005", "Body mass index 30+ - obesity (finding)"))
          .put("N18.1", Pair.of("431855005", "Chronic kidney disease stage 1 (disorder)"))
          .put("D40.0", Pair.of("126906006", "Neoplasm of prostate"))
          .put("I11.0", Pair.of("38341003", "Hypertension"))
          .put("E11.8", Pair.of("237602007", "Metabolic syndrome X (disorder)"))
          .put("E10.9", Pair.of("44054006", "Diabetes"))
          .put("J02.8", Pair.of("195662009", "Acute viral pharyngitis (disorder)"))
          .build();

  static DatamartCondition code(Augmentation.Context<DatamartCondition> ctx) {
    if (!ctx.resource().hasIcdCode()) {
      System.out.println("No icd code exists for condition.cdwId: " + ctx.resource().cdwId());
      return ctx.resource();
    }
    if (ICD_TO_SNOMED_MAPPINGS.containsKey(ctx.resource().icd().get().code())) {
      return convertIcdCodetoSnomed(ctx.resource());
    }
    return ctx.resource();
  }

  /* Build a SNOMED code from an ICD */
  private static DatamartCondition convertIcdCodetoSnomed(DatamartCondition condition) {
    condition.snomed(
        Optional.of(
            SnomedCode.builder()
                .code(ICD_TO_SNOMED_MAPPINGS.get(condition.icd().get().code()).getFirst())
                .display(ICD_TO_SNOMED_MAPPINGS.get(condition.icd().get().code()).getSecond())
                .build()));
    return condition;
  }

  public static void main(String[] args) {
    Augmentation.forResources(DatamartCondition.class)
        .whenMatching(Objects::nonNull)
        .transform(ConditionMultipleCodingAugments::code)
        .build()
        .rewriteFiles();
  }
}
