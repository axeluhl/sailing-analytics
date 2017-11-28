package com.sap.sse.security.shared;

public class TenantManagementException extends UserGroupManagementException {
    private static final long serialVersionUID = -679214392489558609L;
    
    public static final String TENANT_DOES_NOT_EXIST = "Tenant does not exist";
    public static final String TENANT_ALREADY_EXISTS = "Tenant already exists";
    
    public TenantManagementException(String message) {
        super(message);
    }
}