package com.sap.sailing.gwt.home.desktop.places.sponsoring;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.AcceptsOneWidget;

public class SponsoringActivity extends AbstractActivity {

    public SponsoringActivity(SponsoringPlace place, SponsoringClientFactory clientFactory) {
    }

    @Override
    public void start(AcceptsOneWidget panel, EventBus eventBus) {
        panel.setWidget(new TabletAndDesktopSponsoringView());
    }

}
