package com.sap.sailing.domain.common;

public class RaceLogEventNotFoundException extends Exception {
    private static final long serialVersionUID = 4595827888350748956L;
   
    public RaceLogEventNotFoundException() {}
    
    public RaceLogEventNotFoundException(String message) {
        super(message);
    }
}
