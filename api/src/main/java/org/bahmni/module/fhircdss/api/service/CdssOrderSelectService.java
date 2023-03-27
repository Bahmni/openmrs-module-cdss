package org.bahmni.module.fhircdss.api.service;

import org.bahmni.module.fhircdss.api.model.alert.CDSCard;
import org.hl7.fhir.r4.model.Bundle;

import java.util.List;

public interface CdssOrderSelectService {

    List<CDSCard> checkContraindications(String service, Bundle bundle);

}
