package org.bahmni.module.fhircdss.api.service.impl;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.api.server.IBundleProvider;
import ca.uhn.fhir.rest.server.SimpleBundleProvider;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Condition;
import org.hl7.fhir.r4.model.ResourceType;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.openmrs.Concept;
import org.openmrs.ConceptName;
import org.openmrs.Encounter;
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

import static org.bahmni.module.fhircdss.api.service.CdssOrderSelectService.CODING_SYSTEM_FOR_OPENMRS_CONCEPT;
import static org.junit.Assert.assertEquals;
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
        when(Context.getLocale()).thenReturn(Locale.getDefault());
    }

    @Test
    public void shouldIncludeActiveDiagnoses_whenPatientHasOneSavedActiveDiagnosis_oneDraftActiveDiagnosis() throws Exception {
        Bundle mockRequestBundle = getMockRequestBundle();
        Patient patient = new Patient();
        patient.setUuid(PATIENT_UUID);
        IBundleProvider iBundleProvider = new SimpleBundleProvider();
        List<Obs> visitDiagnosesObs = getVisitDiagnosesObs(false);

        when(patientService.getPatientByUuid(anyString())).thenReturn(patient);
        when(fhirConditionService.searchConditions(any(), any(), any(), any(), any(), any(), any(), any(), any(),any())).thenReturn(iBundleProvider);
        when(obsService.getObservationsByPersonAndConcept(any(), any())).thenReturn(visitDiagnosesObs);
        when(conceptTranslator.toFhirResource(any())).thenReturn(getCodeableConcept());

        Bundle conditionBundle = conditionsRequestBuilder.build(mockRequestBundle);

        List<Bundle.BundleEntryComponent> resultConditionEntries = conditionBundle.getEntry().stream().filter(entry -> ResourceType.Condition.equals(entry.getResource().getResourceType())).collect(Collectors.toList());
        assertEquals(2, resultConditionEntries.size());
        assertEquals("Malaria (disorder)", ((Condition)resultConditionEntries.get(0).getResource()).getCode().getText());
    }

    @Test
    public void shouldIncludeActiveConditionsAndIncludeCodingSystemForBahmni_whenPatientHasOneSavedActiveCondition_oneDraftActiveCondition() throws Exception {
        Bundle mockRequestBundle = getMockRequestBundle();
        Patient patient = new Patient();
        patient.setUuid(PATIENT_UUID);

        Condition activeConditionResource = new Condition();
        CodeableConcept activeClinicalStatus = new CodeableConcept();
        activeClinicalStatus.setCoding(Collections.singletonList(new Coding("dummy", "active", "active")));
        activeConditionResource.setClinicalStatus(activeClinicalStatus);
        activeConditionResource.getCode().addCoding(new Coding(null, "74827482-4ff0-0305-1990-000000000001", "Malaria"));
        Condition inactiveConditionResource = new Condition();
        CodeableConcept inactiveClinicalStatus = new CodeableConcept();
        inactiveClinicalStatus.setCoding(Collections.singletonList(new Coding("dummy", "history", "history")));
        inactiveConditionResource.setClinicalStatus(inactiveClinicalStatus);

        IBundleProvider iBundleProvider = new SimpleBundleProvider(Arrays.asList(activeConditionResource, inactiveConditionResource));

        when(patientService.getPatientByUuid(anyString())).thenReturn(patient);
        when(fhirConditionService.searchConditions(any(), any(), any(), any(), any(), any(), any(), any(), any(),any())).thenReturn(iBundleProvider);
        when(obsService.getObservationsByPersonAndConcept(any(), any())).thenReturn(Collections.EMPTY_LIST);
        when(conceptService.getConceptByUuid("74827482-4ff0-0305-1990-000000000001")).thenReturn(getMockConcept());

        Bundle conditionBundle = conditionsRequestBuilder.build(mockRequestBundle);

        List<Bundle.BundleEntryComponent> resultConditionEntries = conditionBundle.getEntry().stream().filter(entry -> ResourceType.Condition.equals(entry.getResource().getResourceType())).collect(Collectors.toList());
        assertEquals(2, resultConditionEntries.size());
        assertEquals(CODING_SYSTEM_FOR_OPENMRS_CONCEPT, ((Condition)resultConditionEntries.get(0).getResource()).getCode().getCoding().get(0).getSystem());
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
        assertEquals(1, resultConditionEntries.size());
    }

    @Test
    public void shouldIncludeActiveDiagnosesAndRenameDiagnosisNameToShortName_whenPatientHasOneSavedActiveDiagnosis_oneDraftActiveDiagnosis() throws Exception {
        Bundle mockRequestBundle = getMockRequestBundle();
        Patient patient = new Patient();
        patient.setUuid(PATIENT_UUID);
        IBundleProvider iBundleProvider = new SimpleBundleProvider();
        List<Obs> visitDiagnosesObs = getVisitDiagnosesObs(true);

        when(patientService.getPatientByUuid(anyString())).thenReturn(patient);
        when(fhirConditionService.searchConditions(any(), any(), any(), any(), any(), any(), any(), any(), any(),any())).thenReturn(iBundleProvider);
        when(obsService.getObservationsByPersonAndConcept(any(), any())).thenReturn(visitDiagnosesObs);
        when(conceptTranslator.toFhirResource(any())).thenReturn(getCodeableConcept());

        Bundle conditionBundle = conditionsRequestBuilder.build(mockRequestBundle);

        List<Bundle.BundleEntryComponent> resultConditionEntries = conditionBundle.getEntry().stream().filter(entry -> ResourceType.Condition.equals(entry.getResource().getResourceType())).collect(Collectors.toList());
        assertEquals(2, resultConditionEntries.size());
        assertEquals("Malaria", ((Condition)resultConditionEntries.get(0).getResource()).getCode().getText());
    }

    private Bundle getMockRequestBundle() throws Exception {
        Path path = Paths.get(getClass().getClassLoader()
                .getResource("request_bundle.json").toURI());
        String mockStr = Files.lines(path, StandardCharsets.UTF_8).collect(Collectors.joining("\n"));
        return FhirContext.forR4().newJsonParser().parseResource(Bundle.class, mockStr);
    }

    private List<Obs> getVisitDiagnosesObs(boolean shortNameNeeded) {
        Obs visitDiagnosisObs = new Obs(1);

        Obs codedDiagnosisObs = new Obs(2);
        Concept malariaConcept = new Concept(1);
        ConceptName malariaConceptFQN = new ConceptName("Malaria (disorder)", Locale.getDefault());
        malariaConcept.setFullySpecifiedName(malariaConceptFQN);
        if (shortNameNeeded) {
            ConceptName malariaConceptShortName = new ConceptName("Malaria", Locale.getDefault());
            malariaConcept.setShortName(malariaConceptShortName);
        }
        codedDiagnosisObs.setValueCoded(malariaConcept);

        Concept codedDiagnosisConcept = new Concept(2);
        ConceptName codedDiagnosisConceptName = new ConceptName(CODED_DIAGNOSIS, Locale.getDefault());
        codedDiagnosisConcept.setFullySpecifiedName(codedDiagnosisConceptName);
        codedDiagnosisObs.setConcept(codedDiagnosisConcept);
        Encounter encounter = new Encounter();
        encounter.setUuid("encounter-uuid");
        codedDiagnosisObs.setEncounter(encounter);
        visitDiagnosisObs.addGroupMember(codedDiagnosisObs);
        return Collections.singletonList(visitDiagnosisObs);
    }

    private List<Obs> getInactiveVisitDiagnosesObs() {
        List<Obs> visitDiagnosesObs = getVisitDiagnosesObs(false);

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

    private CodeableConcept getCodeableConcept() {
        CodeableConcept codeableConcept = new CodeableConcept();
        codeableConcept.setText("Malaria (disorder)");
        return codeableConcept;
    }

    private Concept getMockConcept() {
        Concept malariaConcept = new Concept(1);
        ConceptName malariaConceptFQN = new ConceptName("Malaria (disorder)", Locale.getDefault());
        malariaConcept.setFullySpecifiedName(malariaConceptFQN);
        ConceptName malariaConceptShortName = new ConceptName("Malaria", Locale.getDefault());
        malariaConcept.setShortName(malariaConceptShortName);
        malariaConcept.setUuid("74827482-4ff0-0305-1990-000000000001");
        return malariaConcept;
    }
}