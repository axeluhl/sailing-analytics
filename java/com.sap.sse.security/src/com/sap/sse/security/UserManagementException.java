package com.sap.sse.security;

public class UserManagementException extends Exception {

    private static final long serialVersionUID = 7555799541580565866L;
    
    public static final String USER_DOES_NOT_EXIST = "User does not exist.";
    public static final String USER_ALREADY_EXISTS = "User already exists.";
    public static final String INVALID_CREDENTIALS = "Invalid Credentials.";

    public UserManagementException(String message) {
        super(message);
    }
    
    public UserManagementException(String message, Throwable cause) {
        super(message, cause);
    }
}
