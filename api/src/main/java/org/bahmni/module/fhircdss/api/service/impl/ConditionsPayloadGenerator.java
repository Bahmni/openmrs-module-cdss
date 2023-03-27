package org.bahmni.module.fhircdss.api.service.impl;

import ca.uhn.fhir.context.FhirContext;
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
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class ConditionsPayloadGenerator implements PayloadGenerator {

    private static final String VISIT_DIAGNOSES = "Visit Diagnoses";

    private static final String CODED_DIAGNOSIS = "Coded Diagnosis";

    private static final String BAHMNI_DIAGNOSIS_STATUS = "Bahmni Diagnosis Status";

    PatientService patientService;

    ObsService obsService;

    ConceptService conceptService;

    ConceptTranslator conceptTranslator;

    FhirConditionService fhirConditionService;

    @Autowired
    public ConditionsPayloadGenerator(PatientService patientService, ObsService obsService, ConceptService conceptService, ConceptTranslator conceptTranslator, FhirConditionService fhirConditionService) {
        this.patientService = patientService;
        this.obsService = obsService;
        this.conceptService = conceptService;
        this.conceptTranslator = conceptTranslator;
        this.fhirConditionService = fhirConditionService;
    }

    @Override
    public void generate(Bundle requestBundle, CDSRequest cdsRequest) {
        Bundle conditionsBundle = new Bundle();

        String patientUuid = CdssUtils.getPatientUuidFromMedicationRequestEntry(requestBundle);

        addExistingDiagnosesToBundle(conditionsBundle, patientUuid);
        addExistingActiveConditionsToBundle(conditionsBundle, patientUuid);

        addConditionsFromRequest(requestBundle, conditionsBundle);
        cdsRequest.getPrefetch().setConditions(conditionsBundle);
    }

    private void addExistingDiagnosesToBundle(Bundle conditionsBundle, String patientUuid) {
        Patient openmrsPatient = patientService.getPatientByUuid(patientUuid);
        Concept visitDiagnosesConcept = conceptService.getConceptByName(VISIT_DIAGNOSES);

        List<Obs> visitDiagnosesObs = obsService.getObservationsByPersonAndConcept(openmrsPatient.getPerson(), visitDiagnosesConcept);

        for (Obs obsGroup : visitDiagnosesObs) {
            Obs codedDiagnosisObs = getObsFor(obsGroup, CODED_DIAGNOSIS);
            Obs codedDiagnosisStatusObs = getObsFor(obsGroup, BAHMNI_DIAGNOSIS_STATUS);;

            if (codedDiagnosisObs != null && codedDiagnosisStatusObs == null) {
                Condition condition = new Condition();
                Reference reference = new Reference();
                reference.setReference("Patient/" + openmrsPatient.getUuid());
                CodeableConcept codeableConcept = conceptTranslator.toFhirResource(codedDiagnosisObs.getValueCoded());
                condition.setCode(codeableConcept);
                condition.setSubject(reference);
                addEntryToConditionsBundle(conditionsBundle, condition);
            }
        }
    }

    private void addExistingActiveConditionsToBundle(Bundle conditionsBundle, String patientUuid) {
        IParser parser = FhirContext.forR4().newJsonParser();
        parser.setPrettyPrint(true);

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
            Optional<Coding> clinicalStatusOptional = fhirCondition.getClinicalStatus().getCoding().stream().filter(coding -> "Active".equalsIgnoreCase(coding.getDisplay())).findFirst();
            if (clinicalStatusOptional.isPresent()) {
                addEntryToConditionsBundle(conditionsBundle, fhirCondition);
            }
        }
    }

    private void addConditionsFromRequest(Bundle requestBundle, Bundle conditionsBundle) {
        List<Bundle.BundleEntryComponent> conditionEntries = requestBundle.getEntry().stream().filter(entry -> ResourceType.Condition.equals(entry.getResource().getResourceType())).collect(Collectors.toList());

        for (Bundle.BundleEntryComponent conditionEntry : conditionEntries) {
            Condition conditionResource = (Condition) conditionEntry.getResource();
            Optional<Coding> clinicalStatusOptional = conditionResource.getClinicalStatus().getCoding().stream().filter(coding -> "Active".equals(coding.getDisplay())).findFirst();
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

    private Obs getObsFor(Obs obsGroup, String conceptName) {
        Set<Obs> groupMembers = obsGroup.getGroupMembers();
        for (Obs obs : groupMembers) {
            if (obs.getConcept().getName().getName().equals(conceptName)) {
                return obs;
            }
        }
        return null;
    }
}
