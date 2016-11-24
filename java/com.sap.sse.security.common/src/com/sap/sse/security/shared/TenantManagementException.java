package com.sap.sse.security.shared;

import java.io.Serializable;

public class TenantManagementException extends Exception implements Serializable {
    private static final long serialVersionUID = 7555799541580565866L;
    
    public static final String TENANT_DOES_NOT_EXIST = "Tenant does not exist";
    public static final String TENANT_ALREADY_EXISTS = "Tenant already exists";
    
    private final String message;
    
    @Override
    public String getMessage() {
        return message;
    }

    public TenantManagementException(String message) {
        this.message = message;
    }
}