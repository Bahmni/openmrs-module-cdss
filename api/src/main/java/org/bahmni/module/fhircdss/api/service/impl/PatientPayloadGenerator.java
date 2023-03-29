package org.bahmni.module.fhircdss.api.service.impl;

import org.bahmni.module.fhircdss.api.model.request.CDSRequest;
import org.bahmni.module.fhircdss.api.service.PayloadGenerator;
import org.bahmni.module.fhircdss.api.util.CdssUtils;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Patient;
import org.openmrs.module.fhir2.api.FhirPatientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PatientPayloadGenerator implements PayloadGenerator {

    private FhirPatientService fhirPatientService;

    @Autowired
    public PatientPayloadGenerator(FhirPatientService fhirPatientService) {
        this.fhirPatientService = fhirPatientService;
    }

    @Override
    public void generate(Bundle inputBundle, CDSRequest cdsRequest) {
        String patientUuid = CdssUtils.getPatientUuidFromMedicationRequestEntry(inputBundle);
        Patient fhirPatient = fhirPatientService.get(patientUuid);
        cdsRequest.getPrefetch().setPatient(fhirPatient);
    }
}
