package com.sap.sailing.gwt.home.client.shared.sponsoring;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.resources.client.CssResource.Shared;

public interface GlobalResources extends ClientBundle {
    public static final GlobalResources INSTANCE = GWT.create(GlobalResources.class);

    @Source("com/sap/sailing/gwt/home/client/shared/sponsoring/Global.css")
    GlobalCss globalCss();
        
    @Shared
    public interface GlobalCss extends CssResource {
        String grid();
    }
}
