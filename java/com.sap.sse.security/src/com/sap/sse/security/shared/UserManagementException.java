package com.sap.sse.security.shared;

import java.io.Serializable;

public class UserManagementException extends Exception implements Serializable {

    private static final long serialVersionUID = 7555799541580565866L;
    
    public static final String USER_DOES_NOT_EXIST = "User does not exist.";
    public static final String USER_ALREADY_EXISTS = "User already exists.";
    public static final String INVALID_CREDENTIALS = "Invalid Credentials.";

    public UserManagementException() {} // for GWT serialization
    
    private String message;
    
    @Override
    public String getMessage() {
        return message;
    }

    public UserManagementException(String message) {
        this.message = message;
    }
}
