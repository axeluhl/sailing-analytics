package com.sap.sse.security.shared;

import java.io.Serializable;

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
    
    public WrongPermissionFormatException(WildcardPermission permission) {
        this(permission.toString());
    }
}
