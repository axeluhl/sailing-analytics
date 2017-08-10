package com.sap.sse.security;

import java.io.Serializable;

import org.apache.shiro.authz.Permission;

public class WrongPermissionFormatException extends RuntimeException implements Serializable {
    private static final long serialVersionUID = -1971342390698915927L;
    
    private final String permission;
    
    @Override
    public String getMessage() {
        return permission;
    }

    public WrongPermissionFormatException(String permission) {
        this.permission = permission;
    }
    
    public WrongPermissionFormatException(Permission permission) {
        this(permission.toString());
    }
}
