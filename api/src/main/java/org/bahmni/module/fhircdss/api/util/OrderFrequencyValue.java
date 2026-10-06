package org.bahmni.module.fhircdss.api.util;

public class OrderFrequencyValue {

    private final int frequencyCount;
    private final int periodCount;
    private final String periodUnit;

    public OrderFrequencyValue(int frequencyCount, int periodCount, String periodUnit) {
        this.frequencyCount = frequencyCount;
        this.periodCount = periodCount;
        this.periodUnit = periodUnit;
    }

    public static OrderFrequencyValue onceADay() {
        return fromFrequencyPerDay(1.0);
    }

    public static OrderFrequencyValue fromFrequencyPerDay(double frequencyPerDay) {
        if (frequencyPerDay == Math.floor(frequencyPerDay)) {
            return new OrderFrequencyValue((int) frequencyPerDay, 1, "d");
        }
        int periodCount = (int) Math.max(1, Math.round(1.0 / frequencyPerDay));
        return new OrderFrequencyValue(1, periodCount, "d");
    }

    public int getFrequencyCount() {
        return frequencyCount;
    }

    public int getPeriodCount() {
        return periodCount;
    }

    public String getPeriodUnit() {
        return periodUnit;
    }
}