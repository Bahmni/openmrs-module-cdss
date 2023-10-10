package org.bahmni.module.fhircdss.api.util;

import java.util.HashMap;
import java.util.Map;

public enum Frequency {
    IMMEDIATELY("Immediately", 1, 1, "d"),
    ONCE_A_DAY("Once a day", 1, 1, "d"),
    TWICE_A_DAY("Twice a day", 2, 1, "d"),
    THRICE_A_DAY("Thrice a day", 3, 1, "d"),
    FOUR_TIMES_A_DAY("Four times a day", 4, 1, "d"),
    EVERY_HOUR("Every Hour", 1, 1, "h"),
    EVERY_2_HOURS("Every 2 hours", 1, 2, "h"),
    EVERY_3_HOURS("Every 3 hours", 1, 3, "h"),
    EVERY_4_HOURS("Every 4 hours", 1, 4, "h"),
    EVERY_6_HOURS("Every 6 hours", 1, 6, "h"),
    EVERY_8_HOURS("Every 8 hours", 1, 8, "h"),
    EVERY_12_HOURS("Every 12 hours", 1, 12, "h"),
    ON_ALTERNATE_DAYS("On alternate days", 1, 2, "d"),
    ONCE_A_WEEK("Once a week", 1, 1, "wk"),
    TWICE_A_WEEK("Twice a week", 2, 1, "wk"),
    THRICE_A_WEEK("Thrice a week", 3, 1, "wk"),
    EVERY_2_WEEKS("Every 2 weeks", 1, 2, "wk"),
    EVERY_3_WEEKS("Every 3 weeks", 1, 3, "wk"),
    ONCE_A_MONTH("Once a month", 1, 1, "mo"),
    FIVE_TIMES_A_DAY("Five times a day", 5, 1, "d"),
    FOUR_DAYS_A_WEEK("Four days a week", 4, 1, "wk"),
    FIVE_DAYS_A_WEEK("Five days a week", 5, 1, "wk"),
    SIX_DAYS_A_WEEK("Six days a week", 6, 1, "wk");

    private static final Map<String, Frequency> BY_TEXT = new HashMap<>();
    public final String frequencyText;
    public final int frequencyCount;
    public final int periodCount;
    public final String periodUnit;

    static {
        for (Frequency e : values()) {
            BY_TEXT.put(e.frequencyText, e);
        }
    }
    private Frequency(String frequencyText, int frequencyCount, int periodCount, String periodUnit) {
        this.frequencyText = frequencyText;
        this.frequencyCount = frequencyCount;
        this.periodCount = periodCount;
        this.periodUnit = periodUnit;
    }

    public static Frequency valueOfFrequency(String frequencyText) {
        return BY_TEXT.get(frequencyText);
    }

    public String getFrequencyText() {
        return frequencyText;
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
