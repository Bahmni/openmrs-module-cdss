/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at https://www.bahmni.org/license/mplv2hd.
 *
 * Copyright 2026. SNOMED International. SNOMED International is a registered trademark
 * and the SNOMED International graphic logo is a trademark of SNOMED International.
 */

package org.bahmni.module.fhircdss.api.validator;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.bahmni.module.fhircdss.api.exception.CdssException;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Condition;
import org.hl7.fhir.r4.model.MedicationRequest;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.ResourceType;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class BundleRequestValidator {

    private final Log log = LogFactory.getLog(this.getClass());

    public void validate(Bundle bundle) {
        List<Bundle.BundleEntryComponent> medicationEntries = bundle.getEntry().stream().filter(entry -> ResourceType.MedicationRequest.equals(entry.getResource().getResourceType())).collect(Collectors.toList());
        List<Bundle.BundleEntryComponent> conditionEntries = bundle.getEntry().stream().filter(entry -> ResourceType.Condition.equals(entry.getResource().getResourceType())).collect(Collectors.toList());
        List<Bundle.BundleEntryComponent> patientEntries = bundle.getEntry().stream().filter(entry -> ResourceType.Patient.equals(entry.getResource().getResourceType())).collect(Collectors.toList());

        if (medicationEntries.isEmpty() && conditionEntries.isEmpty() && patientEntries.isEmpty()) {
            log.error("There are no medication orders or conditions or patient in the request");
            throw new CdssException("There are no medication orders or conditions or patient in the request");
        }

        for (Bundle.BundleEntryComponent medicationEntry : medicationEntries) {
            MedicationRequest medicationRequest = (MedicationRequest) medicationEntry.getResource();
            Reference subject = medicationRequest.getSubject();
            if (!ResourceType.Patient.toString().equals(subject.getReferenceElement().getResourceType())) {
                log.error("Subject missing in medication orders in the bundle");
                throw new CdssException("Subject missing in medication orders in the bundle");
            }
        }

        for (Bundle.BundleEntryComponent conditionEntry : conditionEntries) {
            Condition conditionResource = (Condition) conditionEntry.getResource();
            Reference subject = conditionResource.getSubject();
            if (!ResourceType.Patient.toString().equals(subject.getReferenceElement().getResourceType())) {
                log.error("Subject missing in condition entry in the bundle");
                throw new CdssException("Subject missing in condition entry in the bundle");
            }
        }

        for (Bundle.BundleEntryComponent patientEntry : patientEntries) {
            Patient patientResource = (Patient) patientEntry.getResource();
            String patientResourceId = patientResource.getId();
            if (StringUtils.isBlank(patientResourceId)) {
                log.error("patient id missing in patient entry in the bundle");
                throw new CdssException("patient id missing in patient entry in the bundle");
            }
        }
    }
}
