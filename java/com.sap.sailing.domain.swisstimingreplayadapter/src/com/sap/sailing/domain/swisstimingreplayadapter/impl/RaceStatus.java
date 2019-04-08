package com.sap.sailing.domain.swisstimingreplayadapter.impl;

public enum RaceStatus {

    ready, armed, running, finished; 
            
    public static RaceStatus byCode(byte code) {
        return values()[code];
    }
    
}
