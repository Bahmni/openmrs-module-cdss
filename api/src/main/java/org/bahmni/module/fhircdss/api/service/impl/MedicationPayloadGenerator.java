package org.bahmni.module.fhircdss.api.service.impl;

import org.bahmni.module.fhircdss.api.model.request.CDSRequest;
import org.bahmni.module.fhircdss.api.service.PayloadGenerator;
import org.bahmni.module.fhircdss.api.util.CdssUtils;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.MedicationRequest;
import org.hl7.fhir.r4.model.ResourceType;
import org.openmrs.CareSetting;
import org.openmrs.Drug;
import org.openmrs.DrugOrder;
import org.openmrs.DrugReferenceMap;
import org.openmrs.Order;
import org.openmrs.OrderType;
import org.openmrs.Patient;
import org.openmrs.api.OrderService;
import org.openmrs.api.PatientService;
import org.openmrs.module.fhir2.api.FhirConceptSourceService;
import org.openmrs.module.fhir2.api.FhirMedicationRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class MedicationPayloadGenerator implements PayloadGenerator {

    private static final String DRUG_ORDER = "Drug order";

    private PatientService patientService;

    private OrderService orderService;

    private FhirConceptSourceService fhirConceptSourceService;

    private FhirMedicationRequestService fhirMedicationRequestService;

    @Autowired
    public MedicationPayloadGenerator(PatientService patientService, OrderService orderService, FhirConceptSourceService fhirConceptSourceService, FhirMedicationRequestService fhirMedicationRequestService) {
        this.patientService = patientService;
        this.orderService = orderService;
        this.fhirConceptSourceService = fhirConceptSourceService;
        this.fhirMedicationRequestService = fhirMedicationRequestService;
    }

    @Override
    public void generate(Bundle requestBundle, CDSRequest cdsRequest) {
        String patientUuid = CdssUtils.getPatientUuidFromMedicationRequestEntry(requestBundle);
        List<Order> activeOrders = getActiveOrders(patientUuid);
        Bundle medicationBundle = getMedicationBundleForActiveOrders(activeOrders);

        addMedicationsFromRequest(requestBundle, medicationBundle);

        cdsRequest.getPrefetch().setDraftMedicationRequests(medicationBundle);
    }

    private List<Order> getActiveOrders(String patientUuid) {
        Patient openmrsPatient = patientService.getPatientByUuid(patientUuid);
        CareSetting careSetting = orderService.getCareSettingByName(CareSetting.CareSettingType.OUTPATIENT.toString());
        OrderType drugOrderType = orderService.getOrderTypeByName(DRUG_ORDER);
        return orderService.getActiveOrders(openmrsPatient, drugOrderType, careSetting, new Date());
    }

    private Bundle getMedicationBundleForActiveOrders(List<Order> activeOrders) {
        Bundle medicationBundle = new Bundle();
        for (Order order : activeOrders) {
            MedicationRequest medicationRequest = fhirMedicationRequestService.get(order.getUuid());
            CodeableConcept codeableConcept = getCodeableConceptForMedicationRequest(order);
            medicationRequest.setMedication(codeableConcept);
            addEntryToMedicationBundle(medicationBundle, medicationRequest);
        }
        return medicationBundle;
    }

    private CodeableConcept getCodeableConceptForMedicationRequest(Order order) {
        CodeableConcept codeableConcept = new CodeableConcept();

        Drug drug = ((DrugOrder) order).getDrug();
        Set<DrugReferenceMap> drugReferenceMaps = drug.getDrugReferenceMaps();
        if (!drugReferenceMaps.isEmpty()) {
            drugReferenceMaps.stream().forEach(drugReferenceMap -> {
                Coding coding = new Coding();
                coding.setCode(drugReferenceMap.getConceptReferenceTerm().getCode());
                coding.setDisplay(drug.getDisplayName());
                coding.setSystem(fhirConceptSourceService.getUrlForConceptSource(drugReferenceMap.getConceptReferenceTerm().getConceptSource()));
                codeableConcept.addCoding(coding);
            });
            codeableConcept.setText(drug.getDisplayName());
        }

        return codeableConcept;
    }

    private void addMedicationsFromRequest(Bundle requestBundle, Bundle medicationBundle) {
        List<Bundle.BundleEntryComponent> medicationEntries = requestBundle.getEntry().stream().filter(entry -> ResourceType.MedicationRequest.equals(entry.getResource().getResourceType())).collect(Collectors.toList());
        medicationEntries.stream().forEach(medicationEntry -> addEntryToMedicationBundle(medicationBundle, (MedicationRequest) medicationEntry.getResource()));
    }

    private void addEntryToMedicationBundle(Bundle medicationBundle, MedicationRequest medicationRequest) {
        Bundle.BundleEntryComponent bundleEntryComponent = new Bundle.BundleEntryComponent();
        bundleEntryComponent.setResource(medicationRequest);
        medicationBundle.addEntry(bundleEntryComponent);
    }
}
