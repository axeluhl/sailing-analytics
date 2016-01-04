package com.sap.sailing.gwt.home.desktop.places.events;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.sap.sailing.gwt.home.communication.event.GetEventListViewAction;
import com.sap.sailing.gwt.home.communication.eventlist.EventListViewDTO;
import com.sap.sailing.gwt.home.desktop.app.DesktopPlacesNavigator;
import com.sap.sailing.gwt.home.shared.app.ActivityCallback;
import com.sap.sailing.gwt.home.shared.app.NavigationPathDisplay;
import com.sap.sailing.gwt.home.shared.app.NavigationPathDisplay.NavigationItem;
import com.sap.sailing.gwt.home.shared.partials.placeholder.Placeholder;
import com.sap.sailing.gwt.home.shared.places.events.EventsPlace;
import com.sap.sailing.gwt.ui.client.StringMessages;

public class EventsActivity extends AbstractActivity {

    private final EventsClientFactory clientFactory;
    private final EventsPlace place;
    private final DesktopPlacesNavigator homePlacesNavigator;

    public EventsActivity(EventsPlace place, EventsClientFactory clientFactory, DesktopPlacesNavigator homePlacesNavigator, NavigationPathDisplay navigationPathDisplay) {
        this.clientFactory = clientFactory;
        this.place = place;
        this.homePlacesNavigator = homePlacesNavigator;
        
        initNavigationPath(navigationPathDisplay);
    }
    
    private void initNavigationPath(NavigationPathDisplay navigationPathDisplay) {
        StringMessages i18n = StringMessages.INSTANCE;
        navigationPathDisplay.showNavigationPath(new NavigationItem(i18n.home(), homePlacesNavigator.getHomeNavigation()),
                new NavigationItem(i18n.events(), homePlacesNavigator.getEventsNavigation()));
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
