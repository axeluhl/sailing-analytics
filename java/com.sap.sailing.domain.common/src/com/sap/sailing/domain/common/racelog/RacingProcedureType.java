package com.sap.sailing.domain.common.racelog;

public enum RacingProcedureType {
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
}
