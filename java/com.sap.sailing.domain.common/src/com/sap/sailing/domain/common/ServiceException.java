package com.sap.sailing.domain.common;

import java.io.Serializable;

public class ServiceException extends Exception implements Serializable {

    private static final long serialVersionUID = 1690510580423647161L;

    public ServiceException() {
    }
    
    public ServiceException(String message) {
        super(message);
    }
}
