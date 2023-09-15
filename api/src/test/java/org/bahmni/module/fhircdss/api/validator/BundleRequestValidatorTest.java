package org.bahmni.module.fhircdss.api.validator;

import ca.uhn.fhir.context.FhirContext;
import org.bahmni.module.fhircdss.api.exception.CdssException;
import org.hl7.fhir.r4.model.Bundle;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Collectors;

public class BundleRequestValidatorTest {

    private final BundleRequestValidator bundleRequestValidator = new BundleRequestValidator();

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test(expected = Test.None.class)
    public void shouldPassWhenPatientReferenceIsPresentInDraftMedication() throws Exception {
        Bundle mockRequestBundle = getMockBundle("request_bundle.json");
        bundleRequestValidator.validate(mockRequestBundle);
    }

    @Test
    public void shouldFailWhenDraftMedicationOrConditionIsMissing() throws Exception {
        Bundle mockRequestBundle = getMockBundle("invalid_bundle_no_medication_no_condition.json");
        thrown.expect(CdssException.class);
        thrown.expectMessage("There are no medication orders or conditions in the request");

        bundleRequestValidator.validate(mockRequestBundle);
    }

    @Test
    public void shouldFailWhenPatientReferenceIsMissingInDraftMedication() throws Exception {
        Bundle mockBundle = getMockBundle("invalid_bundle_no_subject.json");
        thrown.expect(CdssException.class);
        thrown.expectMessage("Subject missing in medication orders in the bundle");

        bundleRequestValidator.validate(mockBundle);
    }

    @Test
    public void shouldFailWhenPatientReferenceIsMissingInDraftCondition() throws Exception {
        Bundle mockBundle = getMockBundle("invalid_bundle_no_subject_in_condition.json");
        thrown.expect(CdssException.class);
        thrown.expectMessage("Subject missing in condition entry in the bundle");

        bundleRequestValidator.validate(mockBundle);
    }

    private Bundle getMockBundle(String fileName) throws URISyntaxException, IOException {
        Path path = Paths.get(getClass().getClassLoader()
                .getResource(fileName).toURI());
        String mockString = Files.lines(path, StandardCharsets.UTF_8).collect(Collectors.joining("\n"));
        return FhirContext.forR4().newJsonParser().parseResource(Bundle.class, mockString);
    }

}