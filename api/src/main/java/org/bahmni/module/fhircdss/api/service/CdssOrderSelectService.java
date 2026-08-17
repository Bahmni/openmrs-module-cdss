/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at https://www.bahmni.org/license/mplv2hd.
 *
 * Copyright 2026. SNOMED International. SNOMED International is a registered trademark
 * and the SNOMED International graphic logo is a trademark of SNOMED International.
 */

package org.bahmni.module.fhircdss.api.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.bahmni.module.fhircdss.api.model.alert.CDSAlert;
import org.hl7.fhir.r4.model.Bundle;
import org.openmrs.annotation.Authorized;

import java.util.List;

public interface CdssOrderSelectService {
    public static final String CDSS_SERVER_BASE_URL_GLOBAL_PROP = "cdss.fhir.baseurl";

    String CODING_SYSTEM_FOR_OPENMRS_CONCEPT = "https://fhir.openmrs.org";

    @Authorized("Execute CDSS")
    List<CDSAlert> validateInteractions(String serviceName, Bundle bundle) throws JsonProcessingException;
}
