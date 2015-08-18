package com.sap.sailing.gwt.home.client.place.event.partials.listNavigation;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;

public interface RegattaNavigationResources extends ClientBundle {
    public static final RegattaNavigationResources INSTANCE = GWT.create(RegattaNavigationResources.class);

    @Source("RegattaNavigation.gss")
    LocalCss css();

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
        String regattanavigation_legend();
        String regattanavigation_legend_item();
        String regattanavigation_legend_itemtracked();
        String regattanavigation_legend_itemuntracked();
        String regattanavigation_legend_itemlive();
        String regattanavigation_legend_itemplanned();
    }
}
