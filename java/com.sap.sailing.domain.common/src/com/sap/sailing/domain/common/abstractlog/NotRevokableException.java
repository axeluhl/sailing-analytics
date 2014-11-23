package com.sap.sailing.domain.common.abstractlog;

public class NotRevokableException extends Exception {
    private static final long serialVersionUID = 6825451915706047105L;

    public NotRevokableException() {
        super();
    }
    
    public NotRevokableException(String msg) {
        super(msg);
    }
}
