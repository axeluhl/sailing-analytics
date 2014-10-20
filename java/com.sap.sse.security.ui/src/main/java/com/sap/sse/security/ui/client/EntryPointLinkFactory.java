package com.sap.sse.security.ui.client;

import java.util.Map;

import com.sap.sse.gwt.client.AbstractEntryPointLinkFactory;

public class EntryPointLinkFactory extends AbstractEntryPointLinkFactory {
    
    public static String createRegistrationLink(Map<String, String> parameters) {
        return createEntryPointLink("/security/ui/Register.html", parameters);
    }
    
    public static String createLoginLink(Map<String, String> parameters) {
        return createEntryPointLink("/security/ui/Login.html", parameters);
    }
    
    public static String createOAuthLink(Map<String, String> parameters) {
        return createEntryPointLink("/security/ui/OAuthLogin.html", parameters);
    }
    
    public static String createUserManagementLink(Map<String, String> parameters) {
        return createEntryPointLink("/security/ui/UserManagement.html", parameters);
    }

    public static String createEmailValidationLink(Map<String, String> parameters) {
        return createEntryPointLink("/security/ui/EmailValidation.html", parameters);
    }
}
