package com.sap.sailing.gwt.home.client.place.event.seriesanalytics;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;

public interface EventSeriesAnalyticsResources extends ClientBundle {
    public static final EventSeriesAnalyticsResources INSTANCE = GWT.create(EventSeriesAnalyticsResources.class);

    @Source("com/sap/sailing/gwt/home/client/place/event/seriesanalytics/EventSeriesAnalytics.css")
    LocalCss css();

    public interface LocalCss extends CssResource {
        String oldLeaderboardPanel();
        String oldMultiLeaderboardPanel();
    }
}
