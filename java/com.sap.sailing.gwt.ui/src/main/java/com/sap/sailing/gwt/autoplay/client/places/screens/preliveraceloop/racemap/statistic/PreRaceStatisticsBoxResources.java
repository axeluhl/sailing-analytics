package com.sap.sailing.gwt.autoplay.client.places.screens.preliveraceloop.racemap.statistic;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.resources.client.DataResource;
import com.google.gwt.resources.client.DataResource.MimeType;
import com.sap.sailing.gwt.home.shared.partials.statistics.StatisticsBoxResources;

public interface PreRaceStatisticsBoxResources extends StatisticsBoxResources {
    public static final PreRaceStatisticsBoxResources INSTANCE = GWT.create(PreRaceStatisticsBoxResources.class);

    @Source("PreRaceStatisticsBox.gss")
    LocalCss css();
    
    @Source("icon-competitors.svg")
    @MimeType("image/svg+xml")
    DataResource competitors();
    
    @Source("icon-legs.svg")
    @MimeType("image/svg+xml")
    DataResource legs();
    
    @Source("icon-length.svg")
    @MimeType("image/svg+xml")
    DataResource length();
    
    @Source("icon-raceviewer.svg")
    @MimeType("image/svg+xml")
    DataResource raceviewer();
    
    @Source("icon-time.svg")
    @MimeType("image/svg+xml")
    DataResource time();
    
    @Source("icon-wind.svg")
    @MimeType("image/svg+xml")
    DataResource wind();

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
