package com.sap.sse.security.shared;

import java.io.Serializable;

public class UnauthorizedException extends Exception implements Serializable {
    private static final long serialVersionUID = -5837956960452598202L;
    
    private final String message;
    
    @Override
    public String getMessage() {
        return message;
    }

    public UnauthorizedException(String message) {
        this.message = message;
    }
}
