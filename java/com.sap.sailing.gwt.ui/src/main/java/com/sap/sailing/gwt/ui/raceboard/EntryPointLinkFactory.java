package com.sap.sailing.gwt.ui.raceboard;

import java.util.HashMap;

import com.google.gwt.http.client.UrlBuilder;
import com.google.gwt.user.client.Window;
import com.sap.sse.gwt.client.AbstractEntryPointLinkFactory;

public class EntryPointLinkFactory extends AbstractEntryPointLinkFactory {
    
    public static final String createUserProfileLink() {
        return createEntryPointLink("/gwt/Home.html", "/user/profile/:", new HashMap<String, String>());
    }
    
    public static final String createEmailValidationLink() {
        return createFullQualifiedLink("/gwt/Home.html", "/user/confirmation/:MAIL_VERIFIED");
    }
    
    public static final String createPasswordResetLink() {
        return createFullQualifiedLink("/gwt/Home.html", "/user/passwordreset/:");
    }
    
    private static String createFullQualifiedLink(String baseUrl, String hash) {
        String path = createEntryPointLink(baseUrl, new HashMap<String, String>());
        UrlBuilder urlBuilder = Window.Location.createUrlBuilder();
        for (String parameter : Window.Location.getParameterMap().keySet()) {
            urlBuilder.removeParameter(parameter);
        }
        return urlBuilder.setPath(path).setHash(hash).buildString();
    }

}
