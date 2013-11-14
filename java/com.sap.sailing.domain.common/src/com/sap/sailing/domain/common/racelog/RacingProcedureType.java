package com.sap.sailing.domain.common.racelog;

import java.util.ArrayList;
import java.util.List;

public enum RacingProcedureType {
    UNKNOWN("Unknown"),
    RRS26("Fix Line Start (RRS26)"),
    GateStart("Gate Start"),
    ESS("\"Extreme Sailing Series\"-Start");
    
    private String displayName;

    private RacingProcedureType(String displayName) {
        this.displayName = displayName;
    }

    @Override 
    public String toString() {
        return displayName;
    }

    public static RacingProcedureType[] validValues() {
        List<RacingProcedureType> validValues = new ArrayList<RacingProcedureType>();
        for (RacingProcedureType type : values()) {
            if (type != RacingProcedureType.UNKNOWN) {
                validValues.add(type);
            }
        }
        return validValues.toArray(new RacingProcedureType[0]);
    }
}
