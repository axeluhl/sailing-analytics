package com.sap.sailing.gwt.home.client.place.events;

import java.util.List;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.sap.sailing.gwt.home.client.shared.placeholder.Placeholder;
import com.sap.sailing.gwt.ui.shared.EventBaseDTO;
import com.sap.sse.gwt.client.mvp.ErrorView;

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

        clientFactory.getSailingService().getPublicEventsOfAllSailingServers(new AsyncCallback<List<EventBaseDTO>>() {
            @Override
            public void onSuccess(List<EventBaseDTO> events) {
                final EventsView eventsView = clientFactory.createEventsView(EventsActivity.this);
                panel.setWidget(eventsView.asWidget());
                Window.setTitle(place.getTitle());

                eventsView.setEvents(events);
            }

            @Override
            public void onFailure(Throwable caught) {
                final ErrorView view = clientFactory.createErrorView("Error while loading the sailing server instances with service getPublicEventsOfAllSailingServers()", caught);
                panel.setWidget(view.asWidget());
            }
        });
    }
}
