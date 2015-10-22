package com.sap.sailing.gwt.home.desktop.places.events;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.sap.sailing.gwt.home.shared.dispatch.ActivityCallback;
import com.sap.sailing.gwt.home.shared.partials.placeholder.Placeholder;
import com.sap.sailing.gwt.home.shared.places.events.EventsPlace;
import com.sap.sailing.gwt.ui.shared.dispatch.event.GetEventListViewAction;
import com.sap.sailing.gwt.ui.shared.eventlist.EventListViewDTO;

public class EventsActivity extends AbstractActivity {

    private final EventsClientFactory clientFactory;
    private final EventsPlace place;

    public EventsActivity(EventsPlace place, EventsClientFactory clientFactory) {
        this.clientFactory = clientFactory;
        this.place = place;
    }

    @Override
    public void start(final AcceptsOneWidget panel, EventBus eventBus) {
        panel.setWidget(new Placeholder());
        clientFactory.getDispatch().execute(new GetEventListViewAction(),
                new ActivityCallback<EventListViewDTO>(clientFactory, panel) {
            @Override
            public void onSuccess(EventListViewDTO eventListView) {
                final EventsView eventsView = clientFactory.createEventsView();
                panel.setWidget(eventsView.asWidget());
                Window.setTitle(place.getTitle());
                eventsView.setEvents(eventListView);
            }
        });
    }
}
