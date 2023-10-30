package org.bahmni.module.fhircdss.api.service.impl;

import org.bahmni.module.fhircdss.api.service.RequestBuilder;
import org.bahmni.module.fhircdss.api.util.CdssUtils;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Patient;
import org.openmrs.module.fhir2.api.FhirPatientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PatientRequestBuilder implements RequestBuilder<Patient> {

    private FhirPatientService fhirPatientService;

    @Autowired
    public PatientRequestBuilder(FhirPatientService fhirPatientService) {
        this.fhirPatientService = fhirPatientService;
    }

    @Override
    public Patient build(Bundle inputBundle) {
        String patientUuid = CdssUtils.getPatientUuidFromRequest(inputBundle);
        return fhirPatientService.get(patientUuid);
    }
}
