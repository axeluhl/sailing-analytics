package com.sap.sailing.gwt.home.client.app.aboutus;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.AcceptsOneWidget;

public class AboutUsActivity extends AbstractActivity {

    public AboutUsActivity(AboutUsPlace place, AboutUsClientFactory clientFactory) {
        // TODO Auto-generated constructor stub
    }

    @Override
    public void start(AcceptsOneWidget panel, EventBus eventBus) {
        panel.setWidget(new AboutUsView());
    }

}
