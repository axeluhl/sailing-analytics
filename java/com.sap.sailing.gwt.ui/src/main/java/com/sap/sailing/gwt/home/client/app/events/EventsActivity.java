package com.sap.sailing.gwt.home.client.app.events;

import java.util.List;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.sap.sailing.gwt.ui.shared.EventDTO;

public class EventsActivity extends AbstractActivity {

    private final EventsClientFactory clientFactory;

    public EventsActivity(EventsPlace place, EventsClientFactory clientFactory) {
        this.clientFactory = clientFactory;
    }

    @Override
    public void start(final AcceptsOneWidget panel, EventBus eventBus) {
        final EventsView eventsView = clientFactory.createEventsView(EventsActivity.this);
        panel.setWidget(eventsView.asWidget());
        clientFactory.getSailingService().getEvents(new AsyncCallback<List<EventDTO>>() {
            @Override
            public void onSuccess(List<EventDTO> events) {
                eventsView.setEvents(events);
            }

            @Override
            public void onFailure(Throwable caught) {
                Window.alert("Shit happens");
            }
        });
    }
    
    void goTo(Place place) {
        clientFactory.getPlaceController().goTo(place);
    }
}
