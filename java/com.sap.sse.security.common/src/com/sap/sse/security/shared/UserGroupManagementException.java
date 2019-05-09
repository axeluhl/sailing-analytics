package com.sap.sse.security.shared;

import java.io.Serializable;

public class UserGroupManagementException extends Exception implements Serializable {
    private static final long serialVersionUID = -1262664226208506392L;
    
    public static final String USER_GROUP_DOES_NOT_EXIST = "User group does not exist";
    public static final String USER_GROUP_ALREADY_EXISTS = "User group already exists";
    
    public UserGroupManagementException(String message) {
        super(message);
    }
}
