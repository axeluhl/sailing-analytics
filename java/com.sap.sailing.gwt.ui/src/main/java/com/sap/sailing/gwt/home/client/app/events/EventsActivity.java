package com.sap.sailing.gwt.home.client.app.events;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.sap.sailing.gwt.home.client.app.event.EventView;
import com.sap.sailing.gwt.ui.shared.EventDTO;

public class EventsActivity extends AbstractActivity {

    private final EventsClientFactory clientFactory;
    private final EventsPlace place;

    public EventsActivity(EventsPlace place, EventsClientFactory clientFactory) {
        this.clientFactory = clientFactory;
        this.place = place;
    }

    @Override
    public void start(AcceptsOneWidget panel, EventBus eventBus) {
        panel.setWidget(new EventView());
        sailingEventsService.getEventById(eventIdParam, new AsyncCallback<EventDTO>() {
            @Override
            public void onSuccess(EventDTO result) {
                event = result;
                getView().setEvent(event);
            }

            @Override
            public void onFailure(Throwable caught) {
                Window.alert("Shit happens");
            }
        });
    }

}
