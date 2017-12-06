package com.sap.sse.security.userstore.mongodb.impl;

public class FieldNames {
    
    public static enum AccessControlList {
        ID,
        DISPLAY_NAME,
        PERMISSION_MAP
    }
    
    public static enum Ownership {
        /**
         * The ID of the owned object
         */
        OBJECT_ID,
        OWNER_USERNAME,
        OBJECT_DISPLAY_NAME,
        TENANT_OWNER_ID
    }
    
    public static enum Role {
        ID,
        NAME,
        PERMISSIONS
    }
    
    public static enum Tenant {
        ID
    }
    
    public static enum UserGroup {
        ID,
        NAME,
        USERS
    }
    
    public static enum User {
        NAME,
        FULLNAME,
        COMPANY,
        LOCALE,
        EMAIL,
        ACCOUNTS,
        ROLES, PERMISSIONS,
        DEFAULT_TENANT,
        EMAIL_VALIDATED,
        PASSWORD_RESET_SECRET,
        VALIDATION_SECRET;
    }
    
    public static enum Settings {
        NAME,
        MAP,
        TYPES,
        VALUES;
    }
    
    public static enum Preferences {
        USERNAME,
        KEYS_AND_VALUES,
        KEY,
        VALUE;
    }
    
    public static enum Account {
        NAME;
    }
    
    public static enum UsernamePassword {
        NAME,
        SALTED_PW,
        SALT;
    }
    
    
}
