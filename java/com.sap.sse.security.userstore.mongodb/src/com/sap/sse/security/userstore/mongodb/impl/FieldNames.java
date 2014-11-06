package com.sap.sse.security.userstore.mongodb.impl;

public class FieldNames {
    
    public static enum User {
        NAME,
        EMAIL,
        ACCOUNTS,
        ROLES,
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
