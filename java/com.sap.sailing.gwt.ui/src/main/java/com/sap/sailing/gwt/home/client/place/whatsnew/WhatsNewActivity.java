package com.sap.sailing.gwt.home.client.place.whatsnew;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.AcceptsOneWidget;

public class WhatsNewActivity extends AbstractActivity {

    public WhatsNewActivity(WhatsNewPlace place, WhatsNewClientFactory clientFactory) {
    }

    @Override
    public void start(AcceptsOneWidget panel, EventBus eventBus) {
        panel.setWidget(new TabletAndDesktopWhatsNewView());
    }

}
