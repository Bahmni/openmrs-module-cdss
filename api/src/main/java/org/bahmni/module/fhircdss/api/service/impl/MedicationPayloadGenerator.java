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

    PatientService patientService;

    OrderService orderService;

    FhirConceptSourceService fhirConceptSourceService;

    FhirMedicationRequestService fhirMedicationRequestService;

    @Autowired
    public MedicationPayloadGenerator(PatientService patientService, OrderService orderService, FhirConceptSourceService fhirConceptSourceService, FhirMedicationRequestService fhirMedicationRequestService) {
        this.patientService = patientService;
        this.orderService = orderService;
        this.fhirConceptSourceService = fhirConceptSourceService;
        this.fhirMedicationRequestService = fhirMedicationRequestService;
    }

    @Override
    public void generate(Bundle bundle, CDSRequest cdsRequest) {
        String patientUuid = CdssUtils.getPatientUuidFromMedicationRequestEntry(bundle);
        Patient openmrsPatient = patientService.getPatientByUuid(patientUuid);

        CareSetting careSetting = orderService.getCareSettingByName(CareSetting.CareSettingType.OUTPATIENT.toString());
        OrderType drugOrderType = orderService.getOrderTypeByName("Drug order");
        List<Order> activeOrders = orderService.getActiveOrders(openmrsPatient, drugOrderType, careSetting, new Date());

        Bundle medicationBundle = new Bundle();

        for (int i = 0; i < activeOrders.size(); i++) {
            MedicationRequest medicationRequest = fhirMedicationRequestService.get(activeOrders.get(i).getUuid());

            CodeableConcept codeableConcept = new CodeableConcept();
            Drug drug = ((DrugOrder) activeOrders.get(i)).getDrug();
            Set<DrugReferenceMap> drugReferenceMaps = drug.getDrugReferenceMaps();
            if (drugReferenceMaps.size() > 0) {
                drugReferenceMaps.stream().forEach(drugReferenceMap -> {
                    Coding coding = new Coding();
                    coding.setCode(drugReferenceMap.getConceptReferenceTerm().getCode());
                    coding.setDisplay(drug.getDisplayName());
                    coding.setSystem(fhirConceptSourceService.getUrlForConceptSource(drugReferenceMap.getConceptReferenceTerm().getConceptSource()));
                    codeableConcept.addCoding(coding);
                });
                codeableConcept.setText(drug.getDisplayName());
                medicationRequest.setMedication(codeableConcept);
            }

            Bundle.BundleEntryComponent bundleEntryComponent = new Bundle.BundleEntryComponent();
            bundleEntryComponent.setResource(medicationRequest);
            medicationBundle.addEntry(bundleEntryComponent);
        }

        List<Bundle.BundleEntryComponent> medicationEntries = bundle.getEntry().stream().filter(entry -> ResourceType.MedicationRequest.equals(entry.getResource().getResourceType())).collect(Collectors.toList());

        for (Bundle.BundleEntryComponent medicationEntry : medicationEntries) {
            Bundle.BundleEntryComponent bundleEntryComponent = new Bundle.BundleEntryComponent();
            bundleEntryComponent.setResource(medicationEntry.getResource());
            medicationBundle.addEntry(bundleEntryComponent);
        }
        cdsRequest.getPrefetch().setDraftMedicationRequests(medicationBundle);
    }
}
