package com.sap.sailing.gwt.home.client.shared.header;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.resources.client.ImageResource;

public interface HeaderResources extends ClientBundle {
    public static final HeaderResources INSTANCE = GWT.create(HeaderResources.class);

    @Source("com/sap/sailing/gwt/home/client/shared/header/Header.css")
    LocalCss css();

    public interface LocalCss extends CssResource {
        String siteheader();
        String logo();
        String sitenavigation();
        String sitenavigation_active();
        String sitenavigation_link();
        String sitenavigation_linksearch();
    }
    
    @Source("com/sap/sailing/gwt/ui/client/images/logo.png")
    ImageResource logo();
}
