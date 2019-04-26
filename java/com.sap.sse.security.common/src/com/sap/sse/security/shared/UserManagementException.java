package com.sap.sse.security.shared;

import java.io.Serializable;

public class UserManagementException extends Exception implements Serializable {

    private static final long serialVersionUID = 7555799541580565866L;
    
    public static final String USER_DOES_NOT_EXIST = "User does not exist";
    public static final String USER_ALREADY_EXISTS = "User already exists";
    public static final String TENANT_ALREADY_EXISTS = "Tenant already exists";
    public static final String INVALID_CREDENTIALS = "Invalid Credentials";
    public static final String PASSWORD_DOES_NOT_MEET_REQUIREMENTS = "Password does not meet requirements";
    public static final String USERNAME_DOES_NOT_MEET_REQUIREMENTS = "Username does not meet requirements";
    public static final String CANNOT_RESET_PASSWORD_WITHOUT_VALIDATED_EMAIL = "Cannot reset password without validated e-mail address";
    public static final String USER_DOESNT_HAVE_PERMISSION = "The user does not have the required permission";

    private final String message;
    
    @Override
    public String getMessage() {
        return message;
    }

    public UserManagementException(String message) {
        this.message = message;
    }
}
