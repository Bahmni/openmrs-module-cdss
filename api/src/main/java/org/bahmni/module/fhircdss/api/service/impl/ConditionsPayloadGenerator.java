package org.bahmni.module.fhircdss.api.service.impl;

import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.rest.api.server.IBundleProvider;
import ca.uhn.fhir.rest.param.ReferenceAndListParam;
import ca.uhn.fhir.rest.param.ReferenceOrListParam;
import ca.uhn.fhir.rest.param.ReferenceParam;
import org.bahmni.module.fhircdss.api.model.request.CDSRequest;
import org.bahmni.module.fhircdss.api.service.PayloadGenerator;
import org.bahmni.module.fhircdss.api.util.CdssUtils;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Condition;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.ResourceType;
import org.openmrs.Concept;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.api.ConceptService;
import org.openmrs.api.ObsService;
import org.openmrs.api.PatientService;
import org.openmrs.module.fhir2.api.FhirConditionService;
import org.openmrs.module.fhir2.api.translators.ConceptTranslator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class ConditionsPayloadGenerator implements PayloadGenerator {

    private static final String VISIT_DIAGNOSES = "Visit Diagnoses";

    private static final String CODED_DIAGNOSIS = "Coded Diagnosis";

    private static final String BAHMNI_DIAGNOSIS_STATUS = "Bahmni Diagnosis Status";

    public static final String STATUS_ACTIVE = "Active";

    private PatientService patientService;

    private ObsService obsService;

    private ConceptService conceptService;

    private ConceptTranslator conceptTranslator;

    private FhirConditionService fhirConditionService;

    @Autowired
    public ConditionsPayloadGenerator(PatientService patientService, ObsService obsService, ConceptService conceptService, ConceptTranslator conceptTranslator, FhirConditionService fhirConditionService) {
        this.patientService = patientService;
        this.obsService = obsService;
        this.conceptService = conceptService;
        this.conceptTranslator = conceptTranslator;
        this.fhirConditionService = fhirConditionService;
    }

    @Override
    public void generate(Bundle inputBundle, CDSRequest cdsRequest) {
        Bundle conditionsBundle = new Bundle();

        String patientUuid = CdssUtils.getPatientUuidFromMedicationRequestEntry(inputBundle);

        addExistingActiveDiagnosesToBundle(conditionsBundle, patientUuid);
        addExistingActiveConditionsToBundle(conditionsBundle, patientUuid);

        addDraftConditionsFromRequestPayload(conditionsBundle, inputBundle);
        cdsRequest.getPrefetch().setConditions(conditionsBundle);
    }

    private void addExistingActiveDiagnosesToBundle(Bundle conditionsBundle, String patientUuid) {
        Patient patient = patientService.getPatientByUuid(patientUuid);
        Concept visitDiagnosesConcept = conceptService.getConceptByName(VISIT_DIAGNOSES);
        List<Obs> visitDiagnosesObs = obsService.getObservationsByPersonAndConcept(patient.getPerson(), visitDiagnosesConcept);

        visitDiagnosesObs.stream().filter(this::isActiveDiagnosis)
                                  .map(this::getCodedDiagnosis)
                                  .filter(Objects::nonNull)
                                  .forEach(codedDiagnosisObs -> {
                                      Condition condition = new Condition();
                                      Reference reference = new Reference();
                                      reference.setReference("Patient/" + patient.getUuid());
                                      CodeableConcept codeableConcept = conceptTranslator.toFhirResource(codedDiagnosisObs.getValueCoded());
                                      condition.setCode(codeableConcept);
                                      condition.setSubject(reference);
                                      addEntryToConditionsBundle(conditionsBundle, condition);
                                  });
    }

    private void addExistingActiveConditionsToBundle(Bundle conditionsBundle, String patientUuid) {
        IParser parser = CdssUtils.getFhirJsonParser();

        ReferenceAndListParam referenceAndListParam = new ReferenceAndListParam();
        ReferenceParam referenceParam = new ReferenceParam();
        referenceParam.setValue(patientUuid);
        referenceAndListParam.addValue(new ReferenceOrListParam().add(referenceParam));

        IBundleProvider iBundleProvider = fhirConditionService.searchConditions(referenceAndListParam, null,
                null, null, null,
                null, null, null, null,
                null);

        for (int i = 0; i < iBundleProvider.getAllResources().size(); i++) {
            Condition fhirCondition = parser.parseResource(Condition.class, parser.encodeResourceToString(iBundleProvider.getAllResources().get(i)));
            Optional<Coding> clinicalStatusOptional = fhirCondition.getClinicalStatus().getCoding().stream().filter(coding -> STATUS_ACTIVE.equalsIgnoreCase(coding.getDisplay())).findFirst();
            if (clinicalStatusOptional.isPresent()) {
                addEntryToConditionsBundle(conditionsBundle, fhirCondition);
            }
        }
    }

    private void addDraftConditionsFromRequestPayload(Bundle conditionsBundle, Bundle requestBundle) {
        List<Bundle.BundleEntryComponent> conditionEntries = requestBundle.getEntry().stream().filter(entry -> ResourceType.Condition.equals(entry.getResource().getResourceType())).collect(Collectors.toList());

        for (Bundle.BundleEntryComponent conditionEntry : conditionEntries) {
            Condition conditionResource = (Condition) conditionEntry.getResource();
            Optional<Coding> clinicalStatusOptional = conditionResource.getClinicalStatus().getCoding().stream().filter(coding -> STATUS_ACTIVE.equals(coding.getDisplay())).findFirst();
            if (clinicalStatusOptional.isPresent()) {
                addEntryToConditionsBundle(conditionsBundle, conditionResource);
            }
        }
    }

    private void addEntryToConditionsBundle(Bundle conditionsBundle, Condition conditionEntry) {
        Bundle.BundleEntryComponent bundleEntryComponent = new Bundle.BundleEntryComponent();
        bundleEntryComponent.setResource(conditionEntry);
        conditionsBundle.addEntry(bundleEntryComponent);
    }

    private Obs getObsFor(Obs visitDiagnosisObsGroup, String conceptName) {
        Optional<Obs> optionalObs = visitDiagnosisObsGroup.getGroupMembers()
                                                    .stream()
                                                    .filter(obs -> obs.getConcept().getName().getName().equals(conceptName))
                                                    .findFirst();
        if (optionalObs.isPresent()) {
            return optionalObs.get();
        }
        return null;
    }

    private boolean isActiveDiagnosis(Obs visitDiagnosisObsGroup) {
        Obs codedDiagnosisStatusObs = getObsFor(visitDiagnosisObsGroup, BAHMNI_DIAGNOSIS_STATUS);
        return codedDiagnosisStatusObs == null;
    }

    private Obs getCodedDiagnosis(Obs visitDiagnosisObsGroup) {
        return getObsFor(visitDiagnosisObsGroup, CODED_DIAGNOSIS);
    }
}
