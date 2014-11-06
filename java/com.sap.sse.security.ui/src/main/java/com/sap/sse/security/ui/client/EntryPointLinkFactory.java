package com.sap.sse.security.ui.client;

import java.util.Map;

import com.google.gwt.http.client.UrlBuilder;
import com.google.gwt.user.client.Window;
import com.sap.sse.gwt.client.AbstractEntryPointLinkFactory;

public class EntryPointLinkFactory extends AbstractEntryPointLinkFactory {
    
    private static final String PARAM_LOCALE = "locale";
    private static final String PARAM_GWT_CODESVR = "gwt.codesvr";

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
    
    private static void addGwtUrlParameters(final Map<String, String> parameters) {
        final String debugParam = Window.Location.getParameter(PARAM_GWT_CODESVR);
        if (debugParam != null) {
            parameters.put(PARAM_GWT_CODESVR, debugParam);
        }
        final String localeParam = Window.Location.getParameter(PARAM_LOCALE);
        if (localeParam != null) {
            parameters.put(PARAM_LOCALE, localeParam);
        }
    }

    /**
     * Produces an absolute base URL for the validation 
     * 
     * @param parameters must be a writable map because debug and locale parameters may be added to the map
     */
    public static String createEmailValidationLink(Map<String, String> parameters) {
        UrlBuilder builder = Window.Location.createUrlBuilder().setPath("/security/ui/EmailValidation.html");
        addGwtUrlParameters(parameters);
        for (Map.Entry<String, String> param : parameters.entrySet()) {
            builder.setParameter(param.getKey(), param.getValue());
        }
        return builder.buildString();
    }

    /**
     * @param parameters must be a writable map because debug and locale parameters may be added to the map
     */
    public static String createPasswordResetLink(Map<String, String> parameters) {
        UrlBuilder builder = Window.Location.createUrlBuilder().setPath("/security/ui/EditProfile.html");
        addGwtUrlParameters(parameters);
        for (Map.Entry<String, String> param : parameters.entrySet()) {
            builder.setParameter(param.getKey(), param.getValue());
        }
        return builder.buildString();
    }
}
