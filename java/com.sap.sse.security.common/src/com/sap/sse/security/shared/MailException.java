package com.sap.sse.security.shared;

import java.io.Serializable;

public class MailException extends Exception implements Serializable {
    private static final long serialVersionUID = 1L;

    public MailException() {}
    
    public MailException(String message) {
        super(message);
    }
}
