package org.bahmni.module.fhircdss.api.service;

import org.bahmni.module.fhircdss.api.model.alert.CDSAlert;
import org.hl7.fhir.r4.model.Bundle;
import org.openmrs.annotation.Authorized;

import java.util.List;

public interface CdssOrderSelectService {
    public static final String CDSS_SERVER_BASE_URL_GLOBAL_PROP = "cdss.fhir.baseurl";

    String CODING_SYSTEM_FOR_OPENMRS_CONCEPT = "https://fhir.openmrs.org";

    @Authorized("Execute CDSS")
    List<CDSAlert> validateInteractions(String serviceName, Bundle bundle);
}
