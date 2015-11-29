package com.sap.sailing.domain.common;

import java.io.Serializable;

public class NotFoundException extends Exception implements Serializable{

    private static final long serialVersionUID = 1690510580423647161L;

    public NotFoundException() {
    }
    
    public NotFoundException(String message) {
        super(message);
    }
}
