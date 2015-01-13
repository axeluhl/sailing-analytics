package com.sap.sailing.domain.common.abstractlog;

import java.io.Serializable;

public class NotRevokableException extends Exception implements Serializable {
    private static final long serialVersionUID = 6825451915706047105L;

    public NotRevokableException() {
        super();
    }
    
    public NotRevokableException(String msg) {
        super(msg);
    }
}
