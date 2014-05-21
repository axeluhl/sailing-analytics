package com.sap.sailing.gwt.home.client.shared;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;

public interface HeaderPanelResources extends ClientBundle {
    public static final HeaderPanelResources INSTANCE = GWT.create(HeaderPanelResources.class);

    @Source("com/sap/sailing/gwt/home/client/shared/HeaderPanel.css")
    LocalCss css();

    public interface LocalCss extends CssResource {
        String siteheader();

        String logo();

        String sitenavigation();

        String languageSelection();
    }
}
