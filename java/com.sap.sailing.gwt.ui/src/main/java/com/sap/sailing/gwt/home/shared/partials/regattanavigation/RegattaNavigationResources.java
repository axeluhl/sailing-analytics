package com.sap.sailing.gwt.home.shared.partials.regattanavigation;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;

public interface RegattaNavigationResources extends ClientBundle {
    public static final RegattaNavigationResources INSTANCE = GWT.create(RegattaNavigationResources.class);

    @Source("RegattaNavigation.gss")
    LocalCss css();

    public interface LocalCss extends CssResource {
        String regattanavigation_legend();
        String regattanavigation_legend_item();
        String regattanavigation_legend_itemtracked();
        String regattanavigation_legend_itemuntracked();
        String regattanavigation_legend_itemlive();
        String regattanavigation_legend_itemplanned();
    }
}
