package org.bahmni.module.fhircdss.api.service;

import org.hl7.fhir.r4.model.Bundle;

public interface RequestBuilder<T> {

    T build(Bundle inputBundle);
}
