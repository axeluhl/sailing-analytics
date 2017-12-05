package com.sap.sailing.gwt.home.desktop.partials.regattanavigation;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.resources.client.ImageResource;
import com.sap.sailing.gwt.home.desktop.resources.SharedDesktopResources;

public interface RegattaNavigationResources extends SharedDesktopResources {
    public static final RegattaNavigationResources INSTANCE = GWT.create(RegattaNavigationResources.class);

    @Source("RegattaNavigation.gss")
    LocalCss css();
    
    @Source("arrow-down-filled-yellow.png")
    ImageResource arrowDownFilledYellow();

    public interface LocalCss extends CssResource {
        String jsdropdown();
        String jsdropdown_head();
        String jsdropdown_content();
        String regattanavigation();
        String regattanavigation_text();
        String regattanavigation_button();
        String regattanavigation_buttonactive();
        String regattanavigation_filter();
        String jsdropdownactive();
        String regattanavigation_filter_dropdown();
        String regattanavigation_filter_text();
        String regattanavigation_filter_current();
        String regattanavigation_filter_dropdown_link();
        String regattanavigation_filter_dropdown_linkactive();
    }
}
