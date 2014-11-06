package com.sap.sailing.gwt.home.client.place.solutions.analytics;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.AcceptsOneWidget;

public class SailingAnalyticsActivity extends AbstractActivity {

    public SailingAnalyticsActivity(SailingAnalyticsPlace place, SailingAnalyticsClientFactory clientFactory) {
    }

    @Override
    public void start(AcceptsOneWidget panel, EventBus eventBus) {
        panel.setWidget(new TabletAndDesktopSailingAnalyticsView());
    }

}
