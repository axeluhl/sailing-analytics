package com.sap.sse.security.shared;

public abstract class UserStoreManagementException extends Exception {
    
    private static final long serialVersionUID = 7638121811978323526L;

    public UserStoreManagementException(String message) {
        super(message);
    }
    
    public UserStoreManagementException() {}
}
