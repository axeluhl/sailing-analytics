package com.sap.sse.security.userstore.mongodb;

public class FieldNames {
    
    public static enum User {
        NAME,
        EMAIL,
        ACCOUNTS,
        ROLES,
        EMAIL_VALIDATED,
        VALIDATION_SECRET;
    }
    
    public static enum Settings {
        NAME,
        MAP,
        TYPES,
        VALUES;
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
