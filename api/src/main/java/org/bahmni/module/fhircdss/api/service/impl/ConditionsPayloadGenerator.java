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
import org.openmrs.Person;
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


    @Autowired
    PatientService patientService;

    @Autowired
    ObsService obsService;

    @Autowired
    ConceptService conceptService;

    @Autowired
    ConceptTranslator conceptTranslator;

    @Autowired
    FhirConditionService fhirConditionService;

    @Override
    public void generate(Bundle bundle, CDSRequest cdsRequest) {
        Bundle conditionsBundle = new Bundle();
        IParser parser = FhirContext.forR4().newJsonParser();
        parser.setPrettyPrint(true);

        String patientUuid = CdssUtils.getPatientUuidFromMedicationRequestEntry(bundle);
        Patient openmrsPatient = patientService.getPatientByUuid(patientUuid);
        Person person = openmrsPatient.getPerson();
        Concept conceptByName = conceptService.getConceptByName("Visit Diagnoses");

        List<Obs> visitDiagnosesObs = obsService.getObservationsByPersonAndConcept(person, conceptByName);

        for (int i = 0; i < visitDiagnosesObs.size(); i++) {
            Obs obsGroup = visitDiagnosesObs.get(i);
            Set<Obs> groupMembers = obsGroup.getGroupMembers();
            Obs codedDiagnosisObs = null;
            Obs codedDiagnosisStatusObs = null;
            for (Obs obs : groupMembers) {
                if (obs.getConcept().getName().getName().equals("Coded Diagnosis")) {
                    codedDiagnosisObs = obs;
                } else if (obs.getConcept().getName().getName().equals("Bahmni Diagnosis Status")) {
                    codedDiagnosisStatusObs = obs;
                }
            }

            if (codedDiagnosisObs != null && codedDiagnosisStatusObs == null) {
                Condition condition = new Condition();
                Reference reference = new Reference();
                reference.setReference("Patient/" + openmrsPatient.getUuid());
                CodeableConcept codeableConcept = conceptTranslator.toFhirResource(codedDiagnosisObs.getValueCoded());
                List<Coding> codingList = codeableConcept.getCoding().stream().filter(coding -> coding.getSystem() != null).collect(Collectors.toList());
                codeableConcept.setCoding(codingList);
                condition.setCode(codeableConcept);
                condition.setSubject(reference);
                Bundle.BundleEntryComponent bundleEntryComponent = new Bundle.BundleEntryComponent();
                bundleEntryComponent.setResource(condition);
                conditionsBundle.addEntry(bundleEntryComponent);
            }
        }

        ReferenceAndListParam referenceAndListParam = new ReferenceAndListParam();
        ReferenceParam referenceParam = new ReferenceParam();
        referenceParam.setValue(openmrsPatient.getUuid());
        referenceAndListParam.addValue(new ReferenceOrListParam().add(referenceParam));

        IBundleProvider iBundleProvider = fhirConditionService.searchConditions(referenceAndListParam, null,
                null, null, null,
                null, null, null, null,
                null);

        for (int i = 0; i < iBundleProvider.getAllResources().size(); i++) {
            Condition fhirCondition = parser.parseResource(Condition.class, parser.encodeResourceToString(iBundleProvider.getAllResources().get(i)));
            Optional<Coding> clinicalStatusOptional = fhirCondition.getClinicalStatus().getCoding().stream().filter(coding -> "Active".equals(coding.getDisplay())).findFirst();
            if (clinicalStatusOptional.isPresent()) {
                Bundle.BundleEntryComponent bundleEntryComponent = new Bundle.BundleEntryComponent();
                bundleEntryComponent.setResource(fhirCondition);
                conditionsBundle.addEntry(bundleEntryComponent);
            }
        }

        List<Bundle.BundleEntryComponent> conditionEntries = bundle.getEntry().stream().filter(entry -> ResourceType.Condition.equals(entry.getResource().getResourceType())).collect(Collectors.toList());

        for (Bundle.BundleEntryComponent conditionEntry : conditionEntries) {
            Condition conditionResource = (Condition) conditionEntry.getResource();
            Optional<Coding> clinicalStatusOptional = conditionResource.getClinicalStatus().getCoding().stream().filter(coding -> "Active".equals(coding.getDisplay())).findFirst();
            if (clinicalStatusOptional.isPresent()) {
                Bundle.BundleEntryComponent bundleEntryComponent = new Bundle.BundleEntryComponent();
                bundleEntryComponent.setResource(conditionEntry.getResource());
                conditionsBundle.addEntry(bundleEntryComponent);
            }
        }
        cdsRequest.getPrefetch().setConditions(conditionsBundle);
    }
}
