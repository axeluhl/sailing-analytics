package com.sap.sailing.domain.common.racelog.tracking;

import java.io.Serializable;

public class MarkAlreadyUsedInRaceException extends Exception implements Serializable {
    private static final long serialVersionUID = 963948633131929332L;
    
    public MarkAlreadyUsedInRaceException(String message, String raceNames) {
        super(message);
        this.raceNames = raceNames;
    }
    
    private final String raceNames;
    
    public String getRaceNames() {
        return raceNames;
    }
    
}
