package com.sap.sailing.gwt.home.desktop.partials.statistics;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;

public interface StatisticsBoxResources extends ClientBundle {
    public static final StatisticsBoxResources INSTANCE = GWT.create(StatisticsBoxResources.class);

    @Source("StatisticsBox.gss")
    LocalCss css();

    public interface LocalCss extends CssResource {
        String box();
        String box_embedded();
        String box_header();
        String box_content();
        String box_content_item();
        String statisticsbox();
        String statisticsbox_item();
        String statisticsbox_item_icon();
        String statisticsbox_item_name();
        String statisticsbox_item_value();
    }
}
