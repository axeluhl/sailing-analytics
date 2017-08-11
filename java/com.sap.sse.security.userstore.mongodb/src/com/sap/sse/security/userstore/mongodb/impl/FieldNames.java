package com.sap.sse.security.userstore.mongodb.impl;

public class FieldNames {
    
    public static enum AccessControlList {
        NAME,
        PERMISSION_MAP
    }
    
    public static enum Ownership {
        NAME,
        OWNER,
        TENANT_OWNER
    }
    
    public static enum Tenant {
        NAME
    }
    
    public static enum UserGroup {
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
