package org.bahmni.module.fhircdss.api.validator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.bahmni.module.fhircdss.api.exception.CdssException;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Condition;
import org.hl7.fhir.r4.model.MedicationRequest;
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

        if (medicationEntries.isEmpty() && conditionEntries.isEmpty()) {
            log.error("There are no medication orders or conditions in the request");
            throw new CdssException("There are no medication orders or conditions in the request");
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
    }
}
