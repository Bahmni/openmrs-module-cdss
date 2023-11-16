package org.bahmni.module.fhircdss.api.service.impl;

import ca.uhn.fhir.context.FhirContext;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Dosage;
import org.hl7.fhir.r4.model.MedicationRequest;
import org.hl7.fhir.r4.model.Quantity;
import org.hl7.fhir.r4.model.ResourceType;
import org.hl7.fhir.r4.model.Timing;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.Concept;
import org.openmrs.ConceptName;
import org.openmrs.ConceptReferenceTerm;
import org.openmrs.Drug;
import org.openmrs.DrugOrder;
import org.openmrs.DrugReferenceMap;
import org.openmrs.Order;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.OrderService;
import org.openmrs.api.PatientService;
import org.openmrs.api.context.Context;
import org.openmrs.module.fhir2.api.FhirConceptSourceService;
import org.openmrs.module.fhir2.api.FhirMedicationRequestService;

import java.math.BigDecimal;
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

    @Mock
    private AdministrationService administrationService;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void shouldIncludeActiveMedications_whenPatientHasOneActiveMedication_oneDraftMedication() throws Exception {
        Bundle mockRequestBundle = getMockRequestBundle("request_bundle.json");
        MedicationRequest medicationRequest = new MedicationRequest();
        addDummyDosageInstruction(medicationRequest, "ml", 2.0, "Once a day");

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
        Bundle mockRequestBundle = getMockRequestBundle("request_bundle.json");

        when(orderService.getActiveOrders(any(), any(), any(), any())).thenReturn(Collections.emptyList());
        Bundle medicationBundle = medicationRequestBuilder.build(mockRequestBundle);

        List<Bundle.BundleEntryComponent> medicationEntries = medicationBundle.getEntry().stream().filter(entry -> ResourceType.MedicationRequest.equals(entry.getResource().getResourceType())).collect(Collectors.toList());
        List<Bundle.BundleEntryComponent> inputMedicationEntries = mockRequestBundle.getEntry().stream().filter(entry -> ResourceType.MedicationRequest.equals(entry.getResource().getResourceType())).collect(Collectors.toList());

        assertEquals(1, medicationEntries.size());
        assertEquals(inputMedicationEntries.size(), medicationEntries.size());
    }
    @Test
    public void shouldResolveToStandardFhirDoseUnitAndRoute_whenValidMedicationsWithDoseUnitAndRouteInputPassed() throws Exception {
        Bundle mockRequestBundle = getMockRequestBundle("request_bundle_with_multiple_dose_units_and_dose_route.json");
        String initialDoseUnit1 = getDoseUnitFromBundleEntry(mockRequestBundle.getEntry().get(0));
        String initialDoseUnit2 = getDoseUnitFromBundleEntry(mockRequestBundle.getEntry().get(1));
        String initialDoseUnit3 = getDoseUnitFromBundleEntry(mockRequestBundle.getEntry().get(2));
        String initialDoseUnit4 = getDoseUnitFromBundleEntry(mockRequestBundle.getEntry().get(3));
        String initialDoseUnit5 = getDoseUnitFromBundleEntry(mockRequestBundle.getEntry().get(4));
        String initialDoseUnit6 = getDoseUnitFromBundleEntry(mockRequestBundle.getEntry().get(5));
        String initialDoseUnit7 = getDoseUnitFromBundleEntry(mockRequestBundle.getEntry().get(6));
        String initialDoseUnit8 = getDoseUnitFromBundleEntry(mockRequestBundle.getEntry().get(7));
        String initialDoseUnit9 = getDoseUnitFromBundleEntry(mockRequestBundle.getEntry().get(8));
        String initialDoseUnit10 = getDoseUnitFromBundleEntry(mockRequestBundle.getEntry().get(9));

        String initialRoute1 = getRouteOfAdministrationFromBundleEntry(mockRequestBundle.getEntry().get(0));
        String initialRoute2 = getRouteOfAdministrationFromBundleEntry(mockRequestBundle.getEntry().get(1));
        String initialRoute3 = getRouteOfAdministrationFromBundleEntry(mockRequestBundle.getEntry().get(2));
        String initialRoute4 = getRouteOfAdministrationFromBundleEntry(mockRequestBundle.getEntry().get(3));
        String initialRoute5 = getRouteOfAdministrationFromBundleEntry(mockRequestBundle.getEntry().get(4));
        String initialRoute6 = getRouteOfAdministrationFromBundleEntry(mockRequestBundle.getEntry().get(5));
        String initialRoute7 = getRouteOfAdministrationFromBundleEntry(mockRequestBundle.getEntry().get(6));
        String initialRoute8 = getRouteOfAdministrationFromBundleEntry(mockRequestBundle.getEntry().get(7));
        String initialRoute9 = getRouteOfAdministrationFromBundleEntry(mockRequestBundle.getEntry().get(8));
        String initialRoute10 = getRouteOfAdministrationFromBundleEntry(mockRequestBundle.getEntry().get(9));

        when(orderService.getActiveOrders(any(), any(), any(), any())).thenReturn(Collections.emptyList());
        Bundle medicationBundle = medicationRequestBuilder.build(mockRequestBundle);

        List<Bundle.BundleEntryComponent> resultMedicationEntries = medicationBundle.getEntry().stream().filter(entry -> ResourceType.MedicationRequest.equals(entry.getResource().getResourceType())).collect(Collectors.toList());
        List<Bundle.BundleEntryComponent> inputMedicationEntries = mockRequestBundle.getEntry().stream().filter(entry -> ResourceType.MedicationRequest.equals(entry.getResource().getResourceType())).collect(Collectors.toList());

        assertEquals(10, inputMedicationEntries.size());
        assertEquals(10, resultMedicationEntries.size());
        
        String finalDoseUnit1 = getDoseUnitFromBundleEntry(resultMedicationEntries.get(0));
        String finalDoseUnit2 = getDoseUnitFromBundleEntry(resultMedicationEntries.get(1));
        String finalDoseUnit3 = getDoseUnitFromBundleEntry(resultMedicationEntries.get(2));
        String finalDoseUnit4 = getDoseUnitFromBundleEntry(resultMedicationEntries.get(3));
        String finalDoseUnit5 = getDoseUnitFromBundleEntry(resultMedicationEntries.get(4));
        String finalDoseUnit6 = getDoseUnitFromBundleEntry(resultMedicationEntries.get(5));
        String finalDoseUnit7 = getDoseUnitFromBundleEntry(resultMedicationEntries.get(6));
        String finalDoseUnit8 = getDoseUnitFromBundleEntry(resultMedicationEntries.get(7));
        String finalDoseUnit9 = getDoseUnitFromBundleEntry(resultMedicationEntries.get(8));
        String finalDoseUnit10 = getDoseUnitFromBundleEntry(resultMedicationEntries.get(9));

        String finalRoute1 = getRouteOfAdministrationFromBundleEntry(resultMedicationEntries.get(0));
        String finalRoute2 = getRouteOfAdministrationFromBundleEntry(resultMedicationEntries.get(1));
        String finalRoute3 = getRouteOfAdministrationFromBundleEntry(resultMedicationEntries.get(2));
        String finalRoute4 = getRouteOfAdministrationFromBundleEntry(resultMedicationEntries.get(3));
        String finalRoute5 = getRouteOfAdministrationFromBundleEntry(resultMedicationEntries.get(4));
        String finalRoute6 = getRouteOfAdministrationFromBundleEntry(resultMedicationEntries.get(5));
        String finalRoute7 = getRouteOfAdministrationFromBundleEntry(resultMedicationEntries.get(6));
        String finalRoute8 = getRouteOfAdministrationFromBundleEntry(resultMedicationEntries.get(7));
        String finalRoute9 = getRouteOfAdministrationFromBundleEntry(resultMedicationEntries.get(8));
        String finalRoute10 = getRouteOfAdministrationFromBundleEntry(resultMedicationEntries.get(9));

        assertEquals("mL", finalDoseUnit1);
        assertEquals("ml",initialDoseUnit1);

        assertEquals("Tablet", finalDoseUnit2);
        assertEquals("Tablet(s)",initialDoseUnit2);

        assertEquals("Capsule", finalDoseUnit3);
        assertEquals("Capsule(s)",initialDoseUnit3);

        assertEquals("Actuation", finalDoseUnit4);
        assertEquals("Puff(s)",initialDoseUnit4);

        assertEquals("Drop", finalDoseUnit5);
        assertEquals("Drop",initialDoseUnit5);

        assertEquals("Spoonful", finalDoseUnit6);
        assertEquals("Tablespoon",initialDoseUnit6);

        assertEquals("Spoonful", finalDoseUnit7);
        assertEquals("Teaspoon",initialDoseUnit7);

        assertEquals("Capsule", finalDoseUnit8);
        assertEquals("Unit(s)",initialDoseUnit8);

        assertEquals("Tablet", finalDoseUnit9);
        assertEquals("Tablet",initialDoseUnit9);

        assertEquals("Capsule", finalDoseUnit10);
        assertEquals("Capsule",initialDoseUnit10);

        // route check

        assertEquals("P", finalRoute1);
        assertEquals("Intramuscular",initialRoute1);

        assertEquals("N", finalRoute2);
        assertEquals("Nasal",initialRoute2);

        assertEquals("Instill", finalRoute3);
        assertEquals("Topical",initialRoute3);

        assertEquals("P", finalRoute4);
        assertEquals("Intraosseous",initialRoute4);

        assertEquals("P", finalRoute5);
        assertEquals("Intrathecal",initialRoute5);

        assertEquals("SL", finalRoute6);
        assertEquals("Sub Lingual",initialRoute6);

        assertEquals("R", finalRoute7);
        assertEquals("Rectum",initialRoute7);

        assertEquals("V", finalRoute8);
        assertEquals("Per Vaginal",initialRoute8);

        assertEquals("O", finalRoute9);
        assertEquals("Oral",initialRoute9);

        assertEquals("Inhal", finalRoute10);
        assertEquals("Inhalation",initialRoute10);
    }

    @Test
    public void shouldResolveToStandardFhirFrequency_whenValidMedicationsWithFrequencyTextInputPassed() throws Exception {
        Bundle mockRequestBundle = getMockRequestBundle("request_bundle_with_multiple_frequency_text.json");
        String frequencyText1 = getFrequencyTextFromBundleEntry(mockRequestBundle.getEntry().get(0));
        String frequencyText2 = getFrequencyTextFromBundleEntry(mockRequestBundle.getEntry().get(1));
        String frequencyText3 = getFrequencyTextFromBundleEntry(mockRequestBundle.getEntry().get(2));
        String frequencyText4 = getFrequencyTextFromBundleEntry(mockRequestBundle.getEntry().get(3));
        String frequencyText5 = getFrequencyTextFromBundleEntry(mockRequestBundle.getEntry().get(4));

        when(orderService.getActiveOrders(any(), any(), any(), any())).thenReturn(Collections.emptyList());
        Bundle medicationBundle = medicationRequestBuilder.build(mockRequestBundle);

        List<Bundle.BundleEntryComponent> resultMedicationEntries = medicationBundle.getEntry().stream().filter(entry -> ResourceType.MedicationRequest.equals(entry.getResource().getResourceType())).collect(Collectors.toList());
        List<Bundle.BundleEntryComponent> inputMedicationEntries = mockRequestBundle.getEntry().stream().filter(entry -> ResourceType.MedicationRequest.equals(entry.getResource().getResourceType())).collect(Collectors.toList());

        assertEquals(5, inputMedicationEntries.size());
        assertEquals(5, resultMedicationEntries.size());

        Timing frequencyTiming1 = getFrequencyTimingFromBundleEntry(resultMedicationEntries.get(0));
        Timing frequencyTiming2 = getFrequencyTimingFromBundleEntry(resultMedicationEntries.get(1));
        Timing frequencyTiming3 = getFrequencyTimingFromBundleEntry(resultMedicationEntries.get(2));
        Timing frequencyTiming4 = getFrequencyTimingFromBundleEntry(resultMedicationEntries.get(3));
        Timing frequencyTiming5 = getFrequencyTimingFromBundleEntry(resultMedicationEntries.get(4));

        Timing.TimingRepeatComponent repeat1 = frequencyTiming1.getRepeat();
        assertEquals(2, repeat1.getFrequency());
        assertEquals(0, repeat1.getPeriod().compareTo(new BigDecimal(1)));
        assertEquals("d", repeat1.getPeriodUnit().toCode());
        assertEquals("Twice a day",frequencyText1);

        Timing.TimingRepeatComponent repeat2 = frequencyTiming2.getRepeat();

        assertEquals(1, repeat2.getFrequency());
        assertEquals(0, repeat2.getPeriod().compareTo(new BigDecimal(3)));
        assertEquals("h", repeat2.getPeriodUnit().toCode());
        assertEquals("Every 3 hours",frequencyText2);

        Timing.TimingRepeatComponent repeat3 = frequencyTiming3.getRepeat();

        assertEquals(2, repeat3.getFrequency());
        assertEquals(0, repeat3.getPeriod().compareTo(new BigDecimal(1)));
        assertEquals("wk", repeat3.getPeriodUnit().toCode());
        assertEquals("Twice a week",frequencyText3);

        Timing.TimingRepeatComponent repeat4 = frequencyTiming4.getRepeat();

        assertEquals(1, repeat4.getFrequency());
        assertEquals(0, repeat4.getPeriod().compareTo(new BigDecimal(1)));
        assertEquals("mo", repeat4.getPeriodUnit().toCode());
        assertEquals("Once a month",frequencyText4);

        Timing.TimingRepeatComponent repeat5 = frequencyTiming5.getRepeat();

        assertEquals(1, repeat5.getFrequency());
        assertEquals(0, repeat5.getPeriod().compareTo(new BigDecimal(1)));
        assertEquals("d", repeat5.getPeriodUnit().toCode());
        assertEquals("Immediately",frequencyText5);

    }
    @Test
    public void shouldReplaceDoseUnitAndRouteOfAdministrationWithNA_whenDosageUnitsNotPresentInDosageUnitMapperAndRouteOfAdministrationNotPresentInDosageRouteMapper() throws Exception {
        Bundle mockRequestBundle = getMockRequestBundle("request_bundle_with_missing_dose_units_and_dose_route.json");
        when(orderService.getActiveOrders(any(), any(), any(), any())).thenReturn(Collections.emptyList());
        String initialDoseUnit = getDoseUnitFromBundleEntry(mockRequestBundle.getEntry().get(0));
        String initialRoute = getRouteOfAdministrationFromBundleEntry(mockRequestBundle.getEntry().get(0));
        Bundle medicationBundle = medicationRequestBuilder.build(mockRequestBundle);
        List<Bundle.BundleEntryComponent> resultMedicationEntries = medicationBundle.getEntry().stream().filter(entry -> ResourceType.MedicationRequest.equals(entry.getResource().getResourceType())).collect(Collectors.toList());
        assertEquals(1, resultMedicationEntries.size());
        String finalDoseUnit = getDoseUnitFromBundleEntry(resultMedicationEntries.get(0));
        String finalRoute = getRouteOfAdministrationFromBundleEntry(resultMedicationEntries.get(0));
        assertEquals("dummy", initialDoseUnit);
        assertEquals("NA", finalDoseUnit);
        assertEquals("dummyRoute", initialRoute);
        assertEquals("NA", finalRoute);
    }

    private static String getDoseUnitFromBundleEntry(Bundle.BundleEntryComponent bundleEntryComponent) {
        return ((MedicationRequest) bundleEntryComponent.getResource()).getDosageInstruction().get(0).getDoseAndRate().get(0).getDoseQuantity().getUnit();
    }
    private static String getRouteOfAdministrationFromBundleEntry(Bundle.BundleEntryComponent bundleEntryComponent) {
        return ((MedicationRequest) bundleEntryComponent.getResource()).getDosageInstruction().get(0).getRoute().getText();
    }
    private static Timing getFrequencyTimingFromBundleEntry(Bundle.BundleEntryComponent bundleEntryComponent) {
        return ((MedicationRequest) bundleEntryComponent.getResource()).getDosageInstruction().get(0).getTiming();
    }
    private static String getFrequencyTextFromBundleEntry(Bundle.BundleEntryComponent bundleEntryComponent) {
        return ((MedicationRequest) bundleEntryComponent.getResource()).getDosageInstruction().get(0).getTiming().getCode().getText();
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
        drugOrder.setDose(1.0);
        drugOrder.setDoseUnits(getMockConcept("ml", "ml", false));
        drugOrder.setRoute(getMockConcept("Oral", "PO", false));
        return Collections.singletonList(drugOrder);
    }

    private Bundle getMockRequestBundle(String fileName) throws Exception {
        Path path = Paths.get(getClass().getClassLoader()
                .getResource(fileName).toURI());
        String mockString = Files.lines(path, StandardCharsets.UTF_8).collect(Collectors.joining("\n"));
        return FhirContext.forR4().newJsonParser().parseResource(Bundle.class, mockString);
    }
    private Concept getMockConcept(String longName, String conceptShortName, boolean isSet) {
        Concept concept = new Concept();
        ConceptName fullySpecifiedName = new ConceptName(longName, Context.getLocale());
        ConceptName shortName = new ConceptName(conceptShortName, Context.getLocale());

        concept.setFullySpecifiedName(fullySpecifiedName);
        concept.setShortName(shortName);

        concept.setSet(isSet);
        return concept;
    }
    private void addDummyDosageInstruction(MedicationRequest medicationRequest, String doseUnit, double quantity, String frequencyText) {
        Dosage  dosage = new Dosage();
        Dosage.DosageDoseAndRateComponent dosageDoseAndRateComponent = new Dosage.DosageDoseAndRateComponent();
        Quantity doseQuantity = new Quantity();
        doseQuantity.setUnit(doseUnit);
        doseQuantity.setValue(quantity);
        dosageDoseAndRateComponent.setDose(doseQuantity);
        dosage.addDoseAndRate(dosageDoseAndRateComponent);
        Timing timing = new Timing();
        timing.setCode(getMockCodeableConcept(frequencyText, "dummySystem", "dummyCode"));
        dosage.setTiming(timing);
        dosage.setRoute(getMockCodeableConcept("Oral", "dummySystem", "dummyCode"));
        medicationRequest.setDosageInstruction((Collections.singletonList(dosage)));
    }

    private CodeableConcept getMockCodeableConcept(String frequencyText, String dummySystem, String dummyCode) {
        CodeableConcept codeableConcept = new CodeableConcept();
        Coding coding = new Coding();
        coding.setCode(dummyCode);
        coding.setDisplay(frequencyText);
        coding.setSystem(dummySystem);
        codeableConcept.addCoding(coding);
        codeableConcept.setText(frequencyText);
        return codeableConcept;
    }

}