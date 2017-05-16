package com.sap.sailing.domain.common;

import java.io.Serializable;

public class UnableToCloseDeviceMappingException extends Exception implements Serializable{
    private static final long serialVersionUID = 4595827888350748956L;
   
    public UnableToCloseDeviceMappingException() {}
    
    public UnableToCloseDeviceMappingException(String message) {
        super(message);
    }
}
