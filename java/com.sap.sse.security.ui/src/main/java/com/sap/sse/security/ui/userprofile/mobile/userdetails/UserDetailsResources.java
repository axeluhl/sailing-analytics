package com.sap.sse.security.ui.userprofile.mobile.userdetails;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;

public interface UserDetailsResources extends ClientBundle {
    public static final UserDetailsResources INSTANCE = GWT.create(UserDetailsResources.class);

    @Source("UserDetails.gss")
    LocalCss css();

    public interface LocalCss extends CssResource {
        String userstyles();
        String userstyles_error();
        String userstyles_buttons();
        String userstyles_buttons_button();
        String userstyles_socialseperator();
        String userstyles_socialseperator_text();
        String userdetails();
        String userdetails_save();
    }
}
