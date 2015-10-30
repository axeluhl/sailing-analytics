package com.sap.sailing.domain.common;

public class RaceLogEventNotFoundException extends Exception {
    private static final long serialVersionUID = 4595827888350748956L;
    
    @SuppressWarnings("unused") // required for some serialization frameworks such as GWT RPC
    public RaceLogEventNotFoundException() {}
    
    public RaceLogEventNotFoundException(String message) {
        super(message);
    }
}
