package org.bahmni.module.fhircdss.api.util;

import java.util.HashMap;
import java.util.Map;

public enum UnitMapper {

    L_L("l", "L"),
    ML_ML("ml", "mL"),
    UL_UL("ul", "uL"),
    TABLET_TABLET("Tablet(s)", "Tablet"),
    TABLET_DEFAULT("Tablet", "Tablet"),
    CAPSULE_CAPSULE("Capsule(s)", "Capsule"),
    CAPSULE_DEFAULT("Capsule", "Capsule"),
    PUFF_PUFF("Puff(s)", "Actuation"),
    DROP_DROP("Drop", "Drop"),
    TABLESPOON_SPOONFUL("Tablespoon", "Spoonful"),
    TEASPOON("Teaspoon", "Spoonful"),
    UNIT_CAPSULE("Unit(s)", "Capsule");


    private static final Map<String, String> BY_UNIT = new HashMap<>();

    static {
        for (UnitMapper e : values()) {
            BY_UNIT.put(e.inputUnit, e.getTargetUnit());
        }
    }

    public final String inputUnit;
    public final String targetUnit;

    private UnitMapper(String inputUnit, String targetUnit) {
        this.inputUnit = inputUnit;
        this.targetUnit = targetUnit;
    }

    public static String factorOfConversion(String sourceUnit) {
        return BY_UNIT.get(sourceUnit);
    }

    //getter
    public String getInputUnit() {
        return inputUnit;
    }


    public String getTargetUnit() {
        return targetUnit;
    }
}
