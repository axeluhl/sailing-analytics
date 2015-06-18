package com.sap.sailing.domain.common;


/**
 * Some masterdata for the different handicap systems
 * @author Frank
 *
 */
public enum HandicapSystemMasterdata {
    ORC ("ORC"),
    ORC_CLUB ("ORC Club"),
    ORC_INTERNATIONAL ("ORC International", "ORC Int.");

    private final String displayName;
    private final String[] alternativeNames;

    private HandicapSystemMasterdata(String displayName, String... alternativeNames) {
        this.displayName = displayName;
        this.alternativeNames = alternativeNames;
    }

    private HandicapSystemMasterdata(String displayName) {
        this.displayName = displayName;
        this.alternativeNames = null;
    }

    public static HandicapSystemMasterdata resolveHandicapSystem(String handicapSystemName) {
        String handicapSystemToResolve = unifyHandicapSystemName(handicapSystemName);
        for (HandicapSystemMasterdata handicapSystem : values()) {
            if (unifyHandicapSystemName(handicapSystem.displayName).equals(handicapSystemToResolve)) {
                return handicapSystem;
            } else if (handicapSystem.alternativeNames != null) {
                for (String name : handicapSystem.alternativeNames) {
                    if (unifyHandicapSystemName(name).equals(handicapSystemToResolve)) {
                        return handicapSystem;
                    }
                }
            }
        }
        return null;
    }

    public static String unifyHandicapSystemName(String handicapSystemName) {
        return handicapSystemName == null ? null : handicapSystemName.toUpperCase().replaceAll("\\s+","");
    }
    
    public String getDisplayName() {
        return displayName;
    }

    public String[] getAlternativeNames() {
        return alternativeNames == null ? new String[0] : alternativeNames;
    }

}
