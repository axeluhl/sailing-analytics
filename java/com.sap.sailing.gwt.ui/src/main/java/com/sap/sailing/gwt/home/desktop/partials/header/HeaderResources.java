package com.sap.sailing.gwt.home.desktop.partials.header;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.CssResource;
import com.sap.sse.security.ui.authentication.generic.resource.AuthenticationResources;

public interface HeaderResources extends AuthenticationResources {
    public static final HeaderResources INSTANCE = GWT.create(HeaderResources.class);

    @Source("Header.gss")
    LocalCss css();

    public interface LocalCss extends CssResource {
        String siteheader();
        String siteheader_logo();
        String siteheader_logo_image();
        String siteheader_logo_title();
        String sitenavigation();
        String sitenavigation_link();
        String sitenavigation_linkactive();
        String sitenavigation_search();
        String search();
        String search_textfield();
        String search_button();
        String submit();
        String user_menu_button();
        String loggedin();
        String open();
    }
}
