package com.sap.sse.common.mail;

import java.io.Serializable;

public class MailException extends Exception implements Serializable {
    private static final long serialVersionUID = 1L;

    public MailException() {}
    
    public MailException(String message) {
        super(message);
    }
    
    public MailException(String message, Throwable caught) {
        super(message, caught);
    }
}
