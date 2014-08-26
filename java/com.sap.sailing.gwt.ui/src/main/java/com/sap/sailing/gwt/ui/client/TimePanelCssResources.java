package com.sap.sailing.gwt.ui.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.resources.client.ImageResource;

public interface TimePanelCssResources extends ClientBundle {
    public static final TimePanelCssResources INSTANCE = GWT.create(TimePanelCssResources.class);

    @Source("com/sap/sailing/gwt/ui/client/TimePanel.css")
    TimePanelCss css();
    
    @Source("com/sap/sailing/gwt/ui/client/images/SAP_RV_Settings.png")
    ImageResource settingsButton();

    public interface TimePanelCss extends CssResource {
        String settingsButtonStyle();
        
        String settingsButtonBackgroundImage();
    }
}