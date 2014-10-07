package com.sap.sse.security.userstore.shared;

/**
 * Provides entries for Shiro's [urls] ini section. The enumeration literals are parsed by
 * the <code><SecurityServiceImpl</code> constructor. The naming convention for the literals is
 * 
 * @author Benjamin Ebling
 *
 */
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
