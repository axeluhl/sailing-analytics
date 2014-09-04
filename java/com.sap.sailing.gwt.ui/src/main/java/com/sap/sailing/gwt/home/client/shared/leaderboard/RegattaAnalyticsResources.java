package com.sap.sailing.gwt.home.client.shared.leaderboard;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;

public interface RegattaAnalyticsResources extends ClientBundle {
    public static final RegattaAnalyticsResources INSTANCE = GWT.create(RegattaAnalyticsResources.class);

    @Source("com/sap/sailing/gwt/home/client/shared/leaderboard/RegattaAnalytics.css")
    LocalCss css();

    public interface LocalCss extends CssResource {
        String regattanavigation();
        String oldLeaderboardPanel();
    }
}
