package com.sap.sse.security.userstore.shared;

public enum DefaultSettings {

    URLS_LOGIN("/Login.html"),
    URLS_AUTH_LOGIN("anon"),
    URLS_SERVICE("/../../service/**"),
    URLS_AUTH_SERVICE("anon"),
    URLS_USER_MANAGEMENT("/UserManagement.html"),
    URLS_AUTH_USER_MANAGEMENT("authc");
    
    private String value;
    
    private DefaultSettings(String value) {
        this.value = value;
    }
    public String getValue() {
        return value;
    }
    
    
    
}
