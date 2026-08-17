/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at https://www.bahmni.org/license/mplv2hd.
 *
 * Copyright 2026. SNOMED International. SNOMED International is a registered trademark
 * and the SNOMED International graphic logo is a trademark of SNOMED International.
 */

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
