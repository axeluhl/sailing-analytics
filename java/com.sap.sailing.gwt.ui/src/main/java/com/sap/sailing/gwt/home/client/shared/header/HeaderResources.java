package com.sap.sailing.gwt.home.client.shared.header;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.resources.client.CssResource.Shared;
import com.google.gwt.resources.client.ImageResource;

public interface HeaderResources extends ClientBundle {
    public static final HeaderResources INSTANCE = GWT.create(HeaderResources.class);

    @Source("com/sap/sailing/gwt/home/client/shared/header/Header.gss")
    LocalCss css();

    @Shared
    public interface LocalCss extends CssResource {
        String siteheader();
        String siteheader_full();
        String logo();
        String logotitle();
        String sitenavigation();
        String sitenavigation_link();
        String sitenavigation_linkactive();
        String sitenavigation_search();
        String sitenavigationexpanded();
        String searchsmall();
        String search_textfield();
        String search();
        String search_button();
        String submit();
    }
    
    @Source("com/sap/sailing/gwt/home/images/logo-small@2x.png")
    ImageResource logo();

    @Source("com/sap/sailing/gwt/home/images/search-icon.png")
    ImageResource searchIcon();
}
