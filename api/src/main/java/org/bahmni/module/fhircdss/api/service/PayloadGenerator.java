package org.bahmni.module.fhircdss.api.service;

import org.bahmni.module.fhircdss.api.model.request.CDSRequest;
import org.hl7.fhir.r4.model.Bundle;

public interface PayloadGenerator {

    void generate(Bundle inputBundle, CDSRequest cdsRequest);
}
