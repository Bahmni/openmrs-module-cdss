package org.bahmni.module.fhircdss.api.util;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class OrderFrequencyValueTest {

    @Test
    public void shouldReturnOnceADay_whenFrequencyPerDayIsOne() {
        assertFrequency(OrderFrequencyValue.fromFrequencyPerDay(1.0), 1, 1, "d");
    }

    @Test
    public void shouldReturnNumberOfDosesPerDay_whenFrequencyPerDayIsWholeNumber() {
        assertFrequency(OrderFrequencyValue.fromFrequencyPerDay(2.0), 2, 1, "d");
        assertFrequency(OrderFrequencyValue.fromFrequencyPerDay(3.0), 3, 1, "d");
    }

    @Test
    public void shouldReturnOnceEveryTwoDays_whenFrequencyPerDayIsHalf() {
        assertFrequency(OrderFrequencyValue.fromFrequencyPerDay(0.5), 1, 2, "d");
    }

    @Test
    public void shouldReturnOnceEveryFourDays_whenFrequencyPerDayIsTwiceAWeek() {
        // 2 doses per 7 days = 0.285714285 per day (the value stored in the order_frequency table)
        assertFrequency(OrderFrequencyValue.fromFrequencyPerDay(0.285714285), 1, 4, "d");
    }

    @Test
    public void shouldReturnOnceEverySevenDays_whenFrequencyPerDayIsOnceAWeek() {
        assertFrequency(OrderFrequencyValue.fromFrequencyPerDay(0.142857142), 1, 7, "d");
    }

    @Test
    public void shouldReturnOnceEveryThirtyDays_whenFrequencyPerDayIsOnceAMonth() {
        assertFrequency(OrderFrequencyValue.fromFrequencyPerDay(0.033333333), 1, 30, "d");
    }

    @Test
    public void shouldReturnOnceADay_whenOnceADayFactoryUsed() {
        assertFrequency(OrderFrequencyValue.onceADay(), 1, 1, "d");
    }

    @Test
    public void shouldTreatOnceADaySameAsFromFrequencyPerDayOfOne() {
        OrderFrequencyValue onceADay = OrderFrequencyValue.onceADay();
        OrderFrequencyValue oncePerDay = OrderFrequencyValue.fromFrequencyPerDay(1.0);

        assertEquals(oncePerDay.getFrequencyCount(), onceADay.getFrequencyCount());
        assertEquals(oncePerDay.getPeriodCount(), onceADay.getPeriodCount());
        assertEquals(oncePerDay.getPeriodUnit(), onceADay.getPeriodUnit());
    }

    @Test
    public void shouldNeverReturnPeriodCountLessThanOne_whenFrequencyPerDayIsNotWholeNumber() {
        // 3.5 doses/day is not a whole number; 1 / 3.5 rounds to 0, so the minimum period of 1 is applied
        assertFrequency(OrderFrequencyValue.fromFrequencyPerDay(3.5), 1, 1, "d");
        assertFrequency(OrderFrequencyValue.fromFrequencyPerDay(1.5), 1, 1, "d");
    }

    private void assertFrequency(OrderFrequencyValue value, int expectedFrequency, int expectedPeriod, String expectedUnit) {
        assertNotNull(value);
        assertEquals(expectedFrequency, value.getFrequencyCount());
        assertEquals(expectedPeriod, value.getPeriodCount());
        assertEquals(expectedUnit, value.getPeriodUnit());
    }
}