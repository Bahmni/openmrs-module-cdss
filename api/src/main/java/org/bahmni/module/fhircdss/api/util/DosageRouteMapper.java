package org.bahmni.module.fhircdss.api.util;

import java.util.HashMap;
import java.util.Map;

public enum DosageRouteMapper {
    INTRAMUSCULAR_PARENTERAL( "Intramuscular" , "P" ),
    NASAL_NASAL("Nasal" , "N" ),
    TOPICAL_INSTILL ("Topical", "Instill"),
    INTRAOSSEOUS_PARENTERAL ("Intraosseous" , "P"),
    INTRATHECAL_PARENTERAL ("Intrathecal" , "P" ),
    INTRAPERITONEAL_PARENTERAL("Intraperitoneal" , "P"),
    INTRADERMAL_PARENTERAL ("Intradermal" , "P"),
    NASOGASTRIC_NASAL ("Nasogastric" , "N"),
    SUBLINGUAL_SUBLINGUAL("Sub Lingual" , "SL"),
    PERRECTUM_RECTUM( "Rectum" , "R" ),
    SUB_CUTANEOUS_PARENTERAL("Sub Cutaneous", "P"),
    PERVAGINAL_VAGINAL("Per Vaginal" , "V"),
    ORAL_ORAL("Oral" , "O"),
    INTRAVENOUS_PARENTERAL("Intravenous" , "P"),
    INHALATION_INHAL("Inhalation" , "Inhal");
    private static final Map<String, String> BY_UNIT = new HashMap<>();

    static {
        for (DosageRouteMapper e : values()) {
            BY_UNIT.put(e.inputRoute, e.getTargetRoute());
        }
    }

    public final String inputRoute;
    public final String targetRoute;

    private DosageRouteMapper(String inputRoute, String targetRoute) {
        this.inputRoute = inputRoute;
        this.targetRoute = targetRoute;
    }

    public static String getTargetRoute(String inputRoute) {
        return BY_UNIT.get(inputRoute);
    }

    public String getInputRoute() {
        return inputRoute;
    }


    public String getTargetRoute() {
        return targetRoute;
    }
}
