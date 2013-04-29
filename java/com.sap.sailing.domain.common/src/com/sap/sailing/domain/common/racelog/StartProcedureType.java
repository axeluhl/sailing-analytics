package com.sap.sailing.domain.common.racelog;

public enum StartProcedureType {
    //RRS26("Fix-Line-Start (RRS26)"),
    GateStart("Gate-Line-Start"),
    ESS("\"Extreme Sailing Series\"-Start");
    
    private String displayName;

    private StartProcedureType(String displayName){
        this.displayName = displayName;
    }

    @Override 
    public String toString(){
        return displayName;
    }
}
