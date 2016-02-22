package com.sap.sailing.gwt.home.desktop.partials.userHeader;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;

public interface UserHeaderResources extends ClientBundle {
    public static final UserHeaderResources INSTANCE = GWT.create(UserHeaderResources.class);

    @Source("UserHeader.gss")
    LocalCss css();

    public interface LocalCss extends CssResource {
        String userheader();
        String userheader_intro();
        String userheader_intro_logo();
        String userheader_intro_logo_image();
        String userheader_intro_title();
        String userheader_intro_title_realname();
        String userheader_intro_title_username();
    }
}
