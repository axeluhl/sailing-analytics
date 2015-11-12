package com.sap.sailing.gwt.home.desktop.places.events;

import com.google.gwt.core.client.GWT;
import com.sap.sailing.gwt.home.desktop.app.DesktopPlacesNavigator;
import com.sap.sailing.gwt.home.shared.app.NavigationPathDisplay;
import com.sap.sailing.gwt.home.shared.app.ProvidesNavigationPath;
import com.sap.sailing.gwt.home.shared.places.events.EventsPlace;
import com.sap.sse.gwt.client.mvp.AbstractActivityProxy;

public class EventsActivityProxy extends AbstractActivityProxy implements ProvidesNavigationPath {

    private final EventsClientFactory clientFactory;
    private final EventsPlace place;
    private NavigationPathDisplay navigationPathDisplay;
    private final DesktopPlacesNavigator homePlacesNavigator;

    public EventsActivityProxy(EventsPlace place, EventsClientFactory clientFactory, DesktopPlacesNavigator homePlacesNavigator) {
        this.place = place;
        this.clientFactory = clientFactory;
        this.homePlacesNavigator = homePlacesNavigator;
    }

    @Override
    protected void startAsync() {
        GWT.runAsync(new AbstractRunAsyncCallback() {
            @Override
            public void onSuccess() {
                super.onSuccess(new EventsActivity(place, clientFactory, homePlacesNavigator, navigationPathDisplay));
            }
        });
    }

    @Override
    public void setNavigationPathDisplay(NavigationPathDisplay navigationPathDisplay) {
        this.navigationPathDisplay = navigationPathDisplay;
    }
}
