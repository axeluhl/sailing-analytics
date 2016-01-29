package com.sap.sailing.gwt.ui.raceboard;

import java.util.HashMap;

import com.sap.sse.gwt.client.AbstractEntryPointLinkFactory;

public class EntryPointLinkFactory extends AbstractEntryPointLinkFactory {
    
    public static final String createUserProfileLink() {
        return createEntryPointLink("/gwt/Home.html#/user/profile/:", new HashMap<String, String>());
    }
    
    public static final String createEmailValidationLink() {
        return createEntryPointLink("/gwt/Home.html#/user/confirmation/:MAIL_VERIFIED", new HashMap<String, String>());
    }
    
    public static final String createPasswordResetLink() {
        return createEntryPointLink("/gwt/Home.html#/user/passwordreset/:", new HashMap<String, String>());
    }

}
