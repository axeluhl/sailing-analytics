package com.sap.sse.security.ui.userprofile.mobile.userheader;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;

public interface UserHeaderResources extends ClientBundle {
    public static final UserHeaderResources INSTANCE = GWT.create(UserHeaderResources.class);

    @Source("UserHeader.gss")
    LocalCss css();

    public interface LocalCss extends CssResource {
        String eventheader();
        String eventheaderreduced();
        String eventheader_info_subtitle();
        String eventheaderfixedup();
        String eventheaderfixeddown();
        String eventheadernobox();
        String eventheaderwithinputbox();
        String eventheader_info();
        String sidenavigation_drawer();
        String eventheader_inputwrapper();
        String eventheader_info_title();
        String eventheader_info_title_text();
        String eventheader_info_title_text_label();
        String eventheader_info_subtitlereduced();
        String eventheader_info_subtitle_logo();
        String eventheader_info_subtitle_info();
        String eventheader_info_subtitle_info_date();
        String eventheader_info_subtitle_info_location();
        String eventheaderplaceholder();
        String userheader();
        String quickfinder();
        String quickfinder_select();
    }
}
