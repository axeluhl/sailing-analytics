package com.sap.sailing.gwt.autoplay.client.places.screens.preliveraceloop.racemap.statistic;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;

public interface PreRaceStatisticsBoxResources extends ClientBundle {
    public static final PreRaceStatisticsBoxResources INSTANCE = GWT.create(PreRaceStatisticsBoxResources.class);

    @Source("PreRaceStatisticsBox.gss")
    LocalCss css();

    public interface LocalCss extends CssResource {
        String box();
        String box_header();
        String box_content();
        String box_content_item();
        String statisticsbox();
        String statisticsbox_item();
        String statisticsbox_item_icon();
        String statisticsbox_item_name();
        String statisticsbox_item_value();

        String qrHolder();
    }
}
