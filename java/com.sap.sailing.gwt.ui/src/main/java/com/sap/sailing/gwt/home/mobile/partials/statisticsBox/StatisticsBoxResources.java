package com.sap.sailing.gwt.home.mobile.partials.statisticsBox;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;

public interface StatisticsBoxResources extends ClientBundle {
    public static final StatisticsBoxResources INSTANCE = GWT.create(StatisticsBoxResources.class);

    @Source("StatisticsBox.gss")
    LocalCss css();

    public interface LocalCss extends CssResource {
        String statisticsbox();
        String statisticsbox_content();
        String statisticsbox_content_item();
        String statisticsbox_content_item_value();
        String statisticsbox_content_item_icon();
        String statisticsbox_content_item_name();
    }
}
