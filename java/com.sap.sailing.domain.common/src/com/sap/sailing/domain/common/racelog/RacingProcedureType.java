package com.sap.sailing.domain.common.racelog;

import java.util.ArrayList;
import java.util.List;

/**
 * Used to identify a RacingProcedure's type.
 * 
 * When modifying these values also check res/preferences.xml of racecommittee.app!
 */
public enum RacingProcedureType {
    UNKNOWN("Unknown"),
    RRS26("Fix Line Start (RRS26)"),
    SWC("Sailing World Cup Start"),
    GateStart("Gate Start"),
    ESS("\"Extreme Sailing Series\"-Start"),
    BASIC("Basic Countdown Start"),
    LEAGUE("League Start");
    
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
            if (type != UNKNOWN) {
                validValues.add(type);
            }
        }
        return validValues.toArray(new RacingProcedureType[0]);
    }
}
