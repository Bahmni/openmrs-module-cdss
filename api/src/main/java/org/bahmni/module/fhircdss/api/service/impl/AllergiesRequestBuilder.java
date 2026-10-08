package org.bahmni.module.fhircdss.api.service.impl;

import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.rest.api.server.IBundleProvider;
import ca.uhn.fhir.rest.param.ReferenceAndListParam;
import ca.uhn.fhir.rest.param.ReferenceOrListParam;
import ca.uhn.fhir.rest.param.ReferenceParam;
import lombok.extern.slf4j.Slf4j;
import org.bahmni.module.fhircdss.api.service.RequestBuilder;
import org.bahmni.module.fhircdss.api.util.CdssUtils;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.AllergyIntolerance;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Coding;
import org.openmrs.module.fhir2.api.FhirAllergyIntoleranceService;
import org.openmrs.module.fhir2.api.search.param.FhirAllergyIntoleranceSearchParams;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Slf4j
@Component
public class AllergiesRequestBuilder implements RequestBuilder<Bundle> {

    public static final String STATUS_ACTIVE = "active";

    private FhirAllergyIntoleranceService fhirAllergyIntoleranceService;

    @Autowired
    public AllergiesRequestBuilder(FhirAllergyIntoleranceService fhirAllergyIntoleranceService) {
        this.fhirAllergyIntoleranceService = fhirAllergyIntoleranceService;
    }

    @Override
    public Bundle build(Bundle inputBundle) {
        Bundle allergiesBundle = new Bundle();
        String patientUuid = CdssUtils.getPatientUuidFromRequest(inputBundle);

        IParser parser = CdssUtils.getFhirJsonParser();

        ReferenceAndListParam referenceAndListParam = new ReferenceAndListParam();
        ReferenceParam referenceParam = new ReferenceParam();
        referenceParam.setValue(patientUuid);
        referenceAndListParam.addValue(new ReferenceOrListParam().add(referenceParam));

        FhirAllergyIntoleranceSearchParams searchParams = FhirAllergyIntoleranceSearchParams.builder()
                .patientReference(referenceAndListParam)
                .build();

        IBundleProvider iBundleProvider = fhirAllergyIntoleranceService.searchForAllergies(searchParams);

        for (IBaseResource allergyBaseResource : iBundleProvider.getAllResources()) {
            AllergyIntolerance fhirAllergy = parser.parseResource(AllergyIntolerance.class, parser.encodeResourceToString(allergyBaseResource));
            Optional<Coding> clinicalStatusOptional = fhirAllergy.getClinicalStatus().getCoding().stream()
                    .filter(coding -> STATUS_ACTIVE.equalsIgnoreCase(coding.getDisplay()) || STATUS_ACTIVE.equalsIgnoreCase(coding.getCode()))
                    .findFirst();
            if (clinicalStatusOptional.isPresent()) {
                Bundle.BundleEntryComponent bundleEntryComponent = new Bundle.BundleEntryComponent();
                bundleEntryComponent.setResource(fhirAllergy);
                allergiesBundle.addEntry(bundleEntryComponent);
            }
        }

        return allergiesBundle;
    }
}