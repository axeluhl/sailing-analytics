package com.sap.sailing.gwt.home.client.app.event;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.shared.EventDTO;

public class EventActivity extends AbstractActivity {
    private final SailingServiceAsync sailingService;

    private EventDTO event;
    private String eventIdParam;


    public EventActivity(EventPlace place, EventClientFactory clientFactory) {
        sailingService = GWT.create(SailingServiceAsync.class);
    }

    @Override
    public void start(AcceptsOneWidget panel, EventBus eventBus) {
        panel.setWidget(new EventView());
    }

}
