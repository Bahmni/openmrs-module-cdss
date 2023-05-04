package org.bahmni.module.fhircdss.api.service.impl;

import ca.uhn.fhir.context.FhirContext;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.MedicationRequest;
import org.hl7.fhir.r4.model.ResourceType;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.ConceptReferenceTerm;
import org.openmrs.Drug;
import org.openmrs.DrugOrder;
import org.openmrs.DrugReferenceMap;
import org.openmrs.Order;
import org.openmrs.api.OrderService;
import org.openmrs.api.PatientService;
import org.openmrs.module.fhir2.api.FhirConceptSourceService;
import org.openmrs.module.fhir2.api.FhirMedicationRequestService;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class MedicationRequestBuilderTest {

    @InjectMocks
    private MedicationRequestBuilder medicationRequestBuilder;

    @Mock
    private PatientService patientService;

    @Mock
    private OrderService orderService;

    @Mock
    private FhirConceptSourceService fhirConceptSourceService;

    @Mock
    private FhirMedicationRequestService fhirMedicationRequestService;

    @Test
    public void shouldIncludeActiveMedications_whenPatientHasOneActiveMedication_oneDraftMedication() throws Exception {
        Bundle mockRequestBundle = getMockRequestBundle();
        MedicationRequest medicationRequest = new MedicationRequest();

        List<Order> mockedOrders = getDrugOrders();

        when(orderService.getActiveOrders(any(), any(), any(), any())).thenReturn(mockedOrders);
        when(fhirMedicationRequestService.get(anyString())).thenReturn(medicationRequest);
        Bundle medicationBundle = medicationRequestBuilder.build(mockRequestBundle);

        List<Bundle.BundleEntryComponent> resultMedicationEntries = medicationBundle.getEntry().stream().filter(entry -> ResourceType.MedicationRequest.equals(entry.getResource().getResourceType())).collect(Collectors.toList());
        List<Bundle.BundleEntryComponent> inputMedicationEntries = mockRequestBundle.getEntry().stream().filter(entry -> ResourceType.MedicationRequest.equals(entry.getResource().getResourceType())).collect(Collectors.toList());

        assertEquals(1, inputMedicationEntries.size());
        assertEquals(2, resultMedicationEntries.size());
    }

    @Test
    public void shouldExcludeInactiveMedications_whenPatientHasOneInactiveMedication_oneDraftMedication() throws Exception {
        Bundle mockRequestBundle = getMockRequestBundle();

        when(orderService.getActiveOrders(any(), any(), any(), any())).thenReturn(Collections.emptyList());
        Bundle medicationBundle = medicationRequestBuilder.build(mockRequestBundle);

        List<Bundle.BundleEntryComponent> medicationEntries = medicationBundle.getEntry().stream().filter(entry -> ResourceType.MedicationRequest.equals(entry.getResource().getResourceType())).collect(Collectors.toList());
        List<Bundle.BundleEntryComponent> inputMedicationEntries = mockRequestBundle.getEntry().stream().filter(entry -> ResourceType.MedicationRequest.equals(entry.getResource().getResourceType())).collect(Collectors.toList());

        assertEquals(1, medicationEntries.size());
        assertEquals(inputMedicationEntries.size(), medicationEntries.size());
    }

    private List<Order> getDrugOrders() {
        DrugOrder drugOrder = new DrugOrder();
        drugOrder.setUuid("order-uuid");
        Drug drug = new Drug();
        DrugReferenceMap referenceMap = new DrugReferenceMap();
        ConceptReferenceTerm conceptReferenceTerm = new ConceptReferenceTerm();
        conceptReferenceTerm.setCode("1234");
        referenceMap.setConceptReferenceTerm(conceptReferenceTerm);
        drug.setDrugReferenceMaps(Collections.singleton(referenceMap));
        drugOrder.setDrug(drug);
        return Collections.singletonList(drugOrder);
    }

    private Bundle getMockRequestBundle() throws Exception {
        Path path = Paths.get(getClass().getClassLoader()
                .getResource("request_bundle.json").toURI());
        String mockString = Files.lines(path, StandardCharsets.UTF_8).collect(Collectors.joining("\n"));
        return FhirContext.forR4().newJsonParser().parseResource(Bundle.class, mockString);
    }
}