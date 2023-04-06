package org.bahmni.module.fhircdss.api.service.impl;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.api.server.IBundleProvider;
import ca.uhn.fhir.rest.server.SimpleBundleProvider;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Condition;
import org.hl7.fhir.r4.model.ResourceType;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.openmrs.Concept;
import org.openmrs.ConceptName;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.ConceptService;
import org.openmrs.api.ObsService;
import org.openmrs.api.PatientService;
import org.openmrs.api.context.Context;
import org.openmrs.api.context.UserContext;
import org.openmrs.module.fhir2.api.FhirConditionService;
import org.openmrs.module.fhir2.api.translators.ConceptTranslator;
import org.openmrs.util.LocaleUtility;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.beans.factory.annotation.Qualifier;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest({Context.class, LocaleUtility.class})
@PowerMockIgnore("javax.management.*")
public class ConditionsRequestBuilderTest {

    @InjectMocks
    private ConditionsRequestBuilder conditionsRequestBuilder;

    @Mock
    private PatientService patientService;

    @Mock
    private ObsService obsService;

    @Mock
    private ConceptService conceptService;

    @Mock
    private ConceptTranslator conceptTranslator;

    @Mock
    private FhirConditionService fhirConditionService;

    @Mock
    @Qualifier("adminService")
    AdministrationService administrationService;

    @Mock
    private UserContext userContext;

    private static final String PATIENT_UUID = UUID.randomUUID().toString();

    private static final String CODED_DIAGNOSIS = "Coded Diagnosis";

    private static final String BAHMNI_DIAGNOSIS_STATUS = "Bahmni Diagnosis Status";

    @Before
    public void setUp() {
        PowerMockito.mockStatic(Context.class);
        PowerMockito.mockStatic(LocaleUtility.class);
        when(Context.getAdministrationService()).thenReturn(administrationService);
        when(LocaleUtility.getLocalesInOrder()).thenReturn(Collections.singleton(Locale.getDefault()));
    }

    @Test
    public void shouldIncludeActiveDiagnoses_whenPatientHasOneSavedActiveDiagnosis_oneDraftActiveDiagnosis() throws Exception {
        Bundle mockRequestBundle = getMockRequestBundle();
        Patient patient = new Patient();
        patient.setUuid(PATIENT_UUID);
        IBundleProvider iBundleProvider = new SimpleBundleProvider();
        List<Obs> visitDiagnosesObs = getVisitDiagnosesObs();

        when(patientService.getPatientByUuid(anyString())).thenReturn(patient);
        when(fhirConditionService.searchConditions(any(), any(), any(), any(), any(), any(), any(), any(), any(),any())).thenReturn(iBundleProvider);
        when(obsService.getObservationsByPersonAndConcept(any(), any())).thenReturn(visitDiagnosesObs);

        Bundle conditionBundle = conditionsRequestBuilder.build(mockRequestBundle);

        List<Bundle.BundleEntryComponent> resultConditionEntries = conditionBundle.getEntry().stream().filter(entry -> ResourceType.Condition.equals(entry.getResource().getResourceType())).collect(Collectors.toList());
        Assert.assertEquals(2, resultConditionEntries.size());
    }

    @Test
    public void shouldIncludeActiveConditions_whenPatientHasOneSavedActiveCondition_oneDraftActiveCondition() throws Exception {
        Bundle mockRequestBundle = getMockRequestBundle();
        Patient patient = new Patient();
        patient.setUuid(PATIENT_UUID);

        Condition activeConditionResource = new Condition();
        CodeableConcept activeClinicalStatus = new CodeableConcept();
        activeClinicalStatus.setCoding(Collections.singletonList(new Coding("dummy", "active", "active")));
        activeConditionResource.setClinicalStatus(activeClinicalStatus);
        Condition inactiveConditionResource = new Condition();
        CodeableConcept inactiveClinicalStatus = new CodeableConcept();
        inactiveClinicalStatus.setCoding(Collections.singletonList(new Coding("dummy", "history", "history")));
        inactiveConditionResource.setClinicalStatus(inactiveClinicalStatus);

        IBundleProvider iBundleProvider = new SimpleBundleProvider(Arrays.asList(activeConditionResource, inactiveConditionResource));

        when(patientService.getPatientByUuid(anyString())).thenReturn(patient);
        when(fhirConditionService.searchConditions(any(), any(), any(), any(), any(), any(), any(), any(), any(),any())).thenReturn(iBundleProvider);
        when(obsService.getObservationsByPersonAndConcept(any(), any())).thenReturn(Collections.EMPTY_LIST);

        Bundle conditionBundle = conditionsRequestBuilder.build(mockRequestBundle);

        List<Bundle.BundleEntryComponent> resultConditionEntries = conditionBundle.getEntry().stream().filter(entry -> ResourceType.Condition.equals(entry.getResource().getResourceType())).collect(Collectors.toList());
        Assert.assertEquals(2, resultConditionEntries.size());
    }

    @Test
    public void shouldFilterOutInactiveDiagnoses_whenPatientHasOneInactiveDiagnosis_oneDraftActiveCondition() throws Exception {
        Bundle mockRequestBundle = getMockRequestBundle();
        Patient patient = new Patient();
        patient.setUuid(PATIENT_UUID);
        IBundleProvider iBundleProvider = new SimpleBundleProvider();
        List<Obs> visitDiagnosesObs = getInactiveVisitDiagnosesObs();

        when(patientService.getPatientByUuid(anyString())).thenReturn(patient);
        when(fhirConditionService.searchConditions(any(), any(), any(), any(), any(), any(), any(), any(), any(),any())).thenReturn(iBundleProvider);
        when(obsService.getObservationsByPersonAndConcept(any(), any())).thenReturn(visitDiagnosesObs);

        Bundle conditionBundle = conditionsRequestBuilder.build(mockRequestBundle);

        List<Bundle.BundleEntryComponent> resultConditionEntries = conditionBundle.getEntry().stream().filter(entry -> ResourceType.Condition.equals(entry.getResource().getResourceType())).collect(Collectors.toList());
        Assert.assertEquals(1, resultConditionEntries.size());
    }

    private Bundle getMockRequestBundle() throws Exception {
        Path path = Paths.get(getClass().getClassLoader()
                .getResource("request_bundle.json").toURI());
        String mockStr = Files.lines(path, StandardCharsets.UTF_8).collect(Collectors.joining("\n"));
        return FhirContext.forR4().newJsonParser().parseResource(Bundle.class, mockStr);
    }

    private List<Obs> getVisitDiagnosesObs() {
        Obs visitDiagnosisObs = new Obs(1);

        Obs codedDiagnosisObs = new Obs(2);
        Concept malariaConcept = new Concept(1);
        ConceptName malariaConceptName = new ConceptName("Malaria", Locale.getDefault());
        malariaConcept.setFullySpecifiedName(malariaConceptName);
        codedDiagnosisObs.setValueCoded(malariaConcept);

        Concept codedDiagnosisConcept = new Concept(2);
        ConceptName codedDiagnosisConceptName = new ConceptName(CODED_DIAGNOSIS, Locale.getDefault());
        codedDiagnosisConcept.setFullySpecifiedName(codedDiagnosisConceptName);
        codedDiagnosisObs.setConcept(codedDiagnosisConcept);
        visitDiagnosisObs.addGroupMember(codedDiagnosisObs);
        return Collections.singletonList(visitDiagnosisObs);
    }

    private List<Obs> getInactiveVisitDiagnosesObs() {
        List<Obs> visitDiagnosesObs = getVisitDiagnosesObs();

        Obs codedDiagnosisStatusObs = new Obs(5);
        Concept codedDiagnosisStatusConcept = new Concept(5);
        Concept ruledoutStatusConcept = new Concept(5);
        ConceptName codedDiagnosisStatusConceptName = new ConceptName(BAHMNI_DIAGNOSIS_STATUS, Locale.getDefault());
        codedDiagnosisStatusConcept.setFullySpecifiedName(codedDiagnosisStatusConceptName);
        codedDiagnosisStatusObs.setConcept(codedDiagnosisStatusConcept);
        codedDiagnosisStatusObs.setValueCoded(ruledoutStatusConcept);

        visitDiagnosesObs.get(0).addGroupMember(codedDiagnosisStatusObs);

        return visitDiagnosesObs;
    }
}