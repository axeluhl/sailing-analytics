package com.sap.sse.security.ui.client;

import java.util.Map;

import com.google.gwt.http.client.UrlBuilder;
import com.google.gwt.user.client.Window;
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

    /**
     * Produces an absolute base URL for the validation 
     */
    public static String createEmailValidationLink(Map<String, String> parameters) {
        UrlBuilder builder = Window.Location.createUrlBuilder().setPath("/security/ui/EmailValidation.html");
        for (Map.Entry<String, String> param : parameters.entrySet()) {
            builder.setParameter(param.getKey(), param.getValue());
        }
        return builder.buildString();
    }
}
