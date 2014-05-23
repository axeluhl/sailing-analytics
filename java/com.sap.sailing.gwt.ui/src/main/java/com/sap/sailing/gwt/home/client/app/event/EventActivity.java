package com.sap.sailing.gwt.home.client.app.event;

import java.util.UUID;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.shared.EventDTO;

public class EventActivity extends AbstractActivity {
    private final SailingServiceAsync sailingService;

    private final EventPlace eventPlace;


    public EventActivity(EventPlace place, EventClientFactory clientFactory) {
        sailingService = clientFactory.getSailingService();
        this.eventPlace = place;
    }

    @Override
    public void start(final AcceptsOneWidget panel, final EventBus eventBus) {
        sailingService.getEventById(UUID.fromString(eventPlace.getEventId()), new AsyncCallback<EventDTO>() {
            @Override
            public void onSuccess(EventDTO event) {
                final EventView view = new EventView(event);
                panel.setWidget(view);
            }

            @Override
            public void onFailure(Throwable caught) {
                Window.alert("Shit happens");
            }
        });
    }

}
