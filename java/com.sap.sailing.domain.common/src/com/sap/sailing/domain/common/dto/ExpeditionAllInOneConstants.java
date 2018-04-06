package com.sap.sailing.domain.common.dto;

public final class ExpeditionAllInOneConstants {
    public static final String REQUEST_PARAMETER_IMPORT_MODE = "importMode";
    public static final String REQUEST_PARAMETER_BOAT_CLASS = "boatClass";
    public static final String REQUEST_PARAMETER_REGATTA_NAME = "regattaName";
    public enum ImportMode{
        NEW_COMPETITOR, NEW_EVENT, NEW_RACE;
    }
    
    private ExpeditionAllInOneConstants() {
    }
}
