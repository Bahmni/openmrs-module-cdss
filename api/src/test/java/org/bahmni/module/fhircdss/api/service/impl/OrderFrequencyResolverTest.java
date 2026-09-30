package org.bahmni.module.fhircdss.api.service.impl;

import org.bahmni.module.fhircdss.api.util.OrderFrequencyValue;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.Concept;
import org.openmrs.ConceptName;
import org.openmrs.OrderFrequency;
import org.openmrs.api.OrderService;

import java.util.Collections;
import java.util.Locale;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class OrderFrequencyResolverTest {

    @Mock
    private OrderService orderService;

    @InjectMocks
    private OrderFrequencyResolver orderFrequencyResolver;

    @Test
    public void shouldResolve_whenFrequencyTextMatchesConceptNameExactly() {
        when(orderService.getOrderFrequencies(false))
                .thenReturn(Collections.singletonList(orderFrequency("Twice a week", 0.285714285)));

        OrderFrequencyValue resolved = orderFrequencyResolver.resolve("Twice a week");

        assertFrequency(resolved, 1, 4, "d");
        verify(orderService).getOrderFrequencies(false);
    }

    @Test
    public void shouldResolve_whenFrequencyTextDiffersInCaseAndExtraWhitespace() {
        when(orderService.getOrderFrequencies(false))
                .thenReturn(Collections.singletonList(orderFrequency("Twice a week", 0.285714285)));

        OrderFrequencyValue resolved = orderFrequencyResolver.resolve("  TWICE   a WEEK ");

        assertFrequency(resolved, 1, 4, "d");
    }

    @Test
    public void shouldResolve_whenAnyConceptNameMatches() {
        when(orderService.getOrderFrequencies(false))
                .thenReturn(Collections.singletonList(orderFrequencyWithNames(0.285714285, "Twice a week", "2/week")));

        OrderFrequencyValue resolved = orderFrequencyResolver.resolve("2/week");

        assertFrequency(resolved, 1, 4, "d");
    }

    @Test
    public void shouldReturnNull_whenNoFrequencyMatches() {
        when(orderService.getOrderFrequencies(false))
                .thenReturn(Collections.singletonList(orderFrequency("Once a day", 1.0)));

        assertNull(orderFrequencyResolver.resolve("Twice a week"));
    }

    @Test
    public void shouldReturnNull_whenNoOrderFrequenciesConfigured() {
        when(orderService.getOrderFrequencies(false)).thenReturn(Collections.emptyList());

        assertNull(orderFrequencyResolver.resolve("Twice a week"));
    }

    @Test
    public void shouldReturnNull_whenOrderServiceReturnsNull() {
        when(orderService.getOrderFrequencies(false)).thenReturn(null);

        assertNull(orderFrequencyResolver.resolve("Twice a week"));
    }

    @Test
    public void shouldReturnNull_whenFrequencyTextIsNull() {
        assertNull(orderFrequencyResolver.resolve(null));
        verifyNoInteractions(orderService);
    }

    @Test
    public void shouldReturnNull_whenFrequencyTextIsBlank() {
        assertNull(orderFrequencyResolver.resolve(""));
        assertNull(orderFrequencyResolver.resolve("   "));
        verifyNoInteractions(orderService);
    }

    @Test
    public void shouldSkipFrequencyWithNullFrequencyPerDay() {
        when(orderService.getOrderFrequencies(false))
                .thenReturn(Collections.singletonList(orderFrequency("Twice a week", null)));

        assertNull(orderFrequencyResolver.resolve("Twice a week"));
    }

    @Test
    public void shouldSkipFrequencyWithNullConcept() {
        OrderFrequency frequency = new OrderFrequency();
        frequency.setFrequencyPerDay(0.285714285);
        when(orderService.getOrderFrequencies(false)).thenReturn(Collections.singletonList(frequency));

        assertNull(orderFrequencyResolver.resolve("Twice a week"));
    }

    private OrderFrequency orderFrequency(String conceptName, Double frequencyPerDay) {
        return orderFrequencyWithNames(frequencyPerDay, conceptName);
    }

    private OrderFrequency orderFrequencyWithNames(Double frequencyPerDay, String... names) {
        Concept concept = new Concept();
        for (String name : names) {
            concept.addName(new ConceptName(name, Locale.ENGLISH));
        }
        OrderFrequency orderFrequency = new OrderFrequency();
        orderFrequency.setConcept(concept);
        orderFrequency.setFrequencyPerDay(frequencyPerDay);
        return orderFrequency;
    }

    private void assertFrequency(OrderFrequencyValue value, int expectedFrequency, int expectedPeriod, String expectedUnit) {
        assertNotNull(value);
        assertEquals(expectedFrequency, value.getFrequencyCount());
        assertEquals(expectedPeriod, value.getPeriodCount());
        assertEquals(expectedUnit, value.getPeriodUnit());
    }
}