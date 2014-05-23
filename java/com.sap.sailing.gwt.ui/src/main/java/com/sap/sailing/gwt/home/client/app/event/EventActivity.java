package com.sap.sailing.gwt.home.client.app.event;

import java.util.UUID;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.sap.sailing.gwt.ui.shared.EventDTO;

public class EventActivity extends AbstractActivity {
    private final EventClientFactory clientFactory;

    private final EventPlace eventPlace;


    public EventActivity(EventPlace place, EventClientFactory clientFactory) {
        this.clientFactory = clientFactory;
        this.eventPlace = place;
    }

    @Override
    public void start(final AcceptsOneWidget panel, final EventBus eventBus) {
        clientFactory.getSailingService().getEventById(UUID.fromString(eventPlace.getEventUuidAsString()), new AsyncCallback<EventDTO>() {
            @Override
            public void onSuccess(EventDTO event) {
                final EventView view = clientFactory.createEventView(event);
                panel.setWidget(view.asWidget());
            }

            @Override
            public void onFailure(Throwable caught) {
                Window.alert("Shit happens");
            }
        });
    }

}
