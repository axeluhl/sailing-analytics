package com.sap.sailing.gwt.ui.client.shared.charts;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.resources.client.ImageResource;

public interface ChartCssResources extends ClientBundle {
    public static final ChartCssResources INSTANCE = GWT.create(ChartCssResources.class);

    @Source("com/sap/sailing/gwt/ui/client/shared/charts/Charts.css")
    ChartsCss css();
    
    @Source("com/sap/sailing/gwt/ui/client/images/SAP_RV_Settings.png")
    ImageResource settingsButton();

    public interface ChartsCss extends CssResource {
        String busyIndicatorStyle();

        String busyIndicatorImageStyle();
        
        String settingsButtonBackgroundImage();
        
        String chartStyle();
        
        String toolbar();
    }
}