package org.bahmni.module.fhircdss.api.validator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.ResourceType;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class BundleRequestValidator {

    private final Log log = LogFactory.getLog(this.getClass());

    public void validate(Bundle bundle) {
        List<Bundle.BundleEntryComponent> medicationEntries = bundle.getEntry().stream().filter(entry -> ResourceType.MedicationRequest.equals(entry.getResource().getResourceType())).collect(Collectors.toList());
        if (medicationEntries.size() < 1) {
            log.error("There are no medication orders in the request");
            throw new RuntimeException("There are no medication orders in the request");
        }
    }
}
