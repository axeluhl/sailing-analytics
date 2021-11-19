package com.sap.sse.security.ui.client;

import java.util.Map;

import com.google.gwt.http.client.UrlBuilder;
import com.sap.sse.gwt.client.AbstractEntryPointLinkFactory;
import com.sap.sse.gwt.settings.UrlBuilderUtil;

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
     * 
     * @param parameters must be a writable map because debug and locale parameters may be added to the map
     */
    public static String createEmailValidationLink(Map<String, String> parameters) {
        UrlBuilder builder = UrlBuilderUtil.createUrlBuilderFromCurrentLocationWithCleanParametersAndPath("/security/ui/EmailValidation.html");
        for (Map.Entry<String, String> param : parameters.entrySet()) {
            builder.setParameter(param.getKey(), param.getValue());
        }
        return builder.buildString();
    }

    /**
     * @param parameters must be a writable map because debug and locale parameters may be added to the map
     */
    public static String createPasswordResetLink(Map<String, String> parameters) {
        UrlBuilder builder = UrlBuilderUtil.createUrlBuilderFromCurrentLocationWithCleanParametersAndPath("/security/ui/EditProfile.html");
        for (Map.Entry<String, String> param : parameters.entrySet()) {
            builder.setParameter(param.getKey(), param.getValue());
        }
        return builder.buildString();
    }
}
