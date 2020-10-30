package com.sap.sailing.domain.common.racelog.tracking;

import java.io.Serializable;

public class MarkAlreadyUsedInRaceException extends Exception implements Serializable {
    
    private static final long serialVersionUID = 2040461905745135256L;    
    
    public MarkAlreadyUsedInRaceException() {
        super();
        this.raceNames = "";
    }
    
    public MarkAlreadyUsedInRaceException(String message, String raceNames) {
        super(message);
        this.raceNames = raceNames;
    }
    
    private String raceNames;
    
    public String getRaceNames() {
        return raceNames;
    }
    
}
