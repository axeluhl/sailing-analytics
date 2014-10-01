package com.sap.sailing.gwt.home.client.place.error;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.AcceptsOneWidget;

public class ErrorActivity extends AbstractActivity {

    public ErrorActivity(ErrorPlace place, ErrorClientFactory clientFactory) {
    }

    @Override
    public void start(AcceptsOneWidget panel, EventBus eventBus) {
        // TODO: the error place should get the error message from the place
        panel.setWidget(new TabletAndDesktopErrorView(null, null));
    }

}
