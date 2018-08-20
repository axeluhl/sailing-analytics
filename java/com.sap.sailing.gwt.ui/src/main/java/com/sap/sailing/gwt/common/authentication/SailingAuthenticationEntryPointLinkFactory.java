package com.sap.sailing.gwt.common.authentication;

import java.util.HashMap;

import com.sap.sse.gwt.client.AbstractEntryPointLinkFactory;
import com.sap.sse.gwt.settings.UrlBuilderUtil;
import com.sap.sse.security.ui.authentication.generic.GenericAuthenticationLinkFactory;

public final class SailingAuthenticationEntryPointLinkFactory extends AbstractEntryPointLinkFactory implements
        GenericAuthenticationLinkFactory {
    
    public static final GenericAuthenticationLinkFactory INSTANCE = new SailingAuthenticationEntryPointLinkFactory();
    
    private SailingAuthenticationEntryPointLinkFactory() {
    }
    
    @Override
    public final String createUserProfileLink() {
        return createEntryPointLink("/gwt/Home.html", "/user/profile/:", new HashMap<String, String>());
    }
    
    @Override
    public final String createEmailValidationLink() {
        return createFullQualifiedLink("/gwt/Home.html", "/user/confirmation/:MAIL_VERIFIED");
    }
    
    @Override
    public final String createPasswordResetLink() {
        return createFullQualifiedLink("/gwt/Home.html", "/user/passwordreset/:");
    }
    
    @Override
    public String createMoreInfoAboutLoginLink() {
        return createFullQualifiedLink("/gwt/Home.html", "/about/account/:");
    }
    
    private String createFullQualifiedLink(String path, String hash) {
        return UrlBuilderUtil.createUrlBuilderFromCurrentLocationWithCleanParametersAndPathAndHash(path, hash).buildString();
    }
}
