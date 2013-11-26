package com.sap.sailing.domain.igtimiadapter.datatypes;

public enum Type {
    gps_latlong(1);
    
    public int getCode() {
        return code;
    }
    
    private final int code;

    private Type(int code) {
        this.code = code;
    }
}
