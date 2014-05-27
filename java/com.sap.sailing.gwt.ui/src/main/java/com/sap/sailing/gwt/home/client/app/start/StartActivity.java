package com.sap.sailing.gwt.home.client.app.start;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.AcceptsOneWidget;

public class StartActivity extends AbstractActivity {
    private final StartClientFactory clientFactory;

    public StartActivity(StartPlace place, StartClientFactory clientFactory) {
        this.clientFactory = clientFactory;
    }

    @Override
    public void start(AcceptsOneWidget panel, EventBus eventBus) {
        final StartView view = clientFactory.createStartView();
        panel.setWidget(view.asWidget());
    }

}
