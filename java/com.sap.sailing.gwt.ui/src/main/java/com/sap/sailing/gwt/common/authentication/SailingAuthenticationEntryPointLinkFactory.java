package com.sap.sailing.gwt.common.authentication;

import java.util.HashMap;

import com.google.gwt.http.client.UrlBuilder;
import com.google.gwt.user.client.Window;
import com.sap.sse.gwt.client.AbstractEntryPointLinkFactory;
import com.sap.sse.security.ui.authentication.generic.GenericAuthenticationLinkFactory;

public final class SailingAuthenticationEntryPointLinkFactory extends AbstractEntryPointLinkFactory implements
        GenericAuthenticationLinkFactory {
    
    public static final GenericAuthenticationLinkFactory INSTANCE = new SailingAuthenticationEntryPointLinkFactory();
    
    private SailingAuthenticationEntryPointLinkFactory() {
    }
    
    @Override
    public final String createUserProfileLink() {
        return createEntryPointLink("/gwt/Home.html#/user/profile/:", new HashMap<String, String>());
    }
    
    @Override
    public final String createEmailValidationLink() {
        return createFullQualifiedLink("/gwt/Home.html", "/user/confirmation/:MAIL_VERIFIED");
    }
    
    @Override
    public final String createPasswordResetLink() {
        return createFullQualifiedLink("/gwt/Home.html", "/user/passwordreset/:");
    }
    
    private String createFullQualifiedLink(String baseUrl, String hash) {
        String path = createEntryPointLink(baseUrl, new HashMap<String, String>());
        UrlBuilder urlBuilder = Window.Location.createUrlBuilder();
        for (String parameter : Window.Location.getParameterMap().keySet()) {
            urlBuilder.removeParameter(parameter);
        }
        return urlBuilder.setPath(path).setHash(hash).buildString();
    }

}
