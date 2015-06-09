package com.sap.sailing.gwt.home.mobile.places.events;

import java.util.UUID;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.sap.sailing.gwt.home.client.place.event.legacy.EventPlace;
import com.sap.sailing.gwt.home.client.place.events.EventsPlace;
import com.sap.sailing.gwt.home.mobile.app.ApplicationContext;
import com.sap.sailing.gwt.home.mobile.places.events.EventsView.Presenter;

public class EventsActivity extends AbstractActivity implements Presenter {
    private final ApplicationContext clientFactory;
    private final EventsPlace place;

    public EventsActivity(EventsPlace place, ApplicationContext clientFactory) {
        this.clientFactory = clientFactory;
        this.place = place;
    }

    @Override
    public void start(final AcceptsOneWidget panel, EventBus eventBus) {
        final EventsView view = new EventsViewImpl(this);
        panel.setWidget(view.asWidget());
        Window.setTitle(place.getTitle());
    }

    @Override
    public void gotoTheEvent(UUID eventId) {
        // TODO: load all we need
        clientFactory.goToPlace(new EventPlace(""));
    }
}
