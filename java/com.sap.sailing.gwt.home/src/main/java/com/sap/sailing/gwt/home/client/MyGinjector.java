package com.sap.sailing.gwt.home.client;

import com.google.gwt.inject.client.AsyncProvider;
import com.google.gwt.inject.client.GinModules;
import com.google.gwt.inject.client.Ginjector;
import com.google.inject.Provider;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.proxy.PlaceManager;

@GinModules({ MyModule.class })
public interface MyGinjector extends Ginjector {
    PlaceManager getPlaceManager();

    EventBus getEventBus();

    Provider<RootPagePresenter> getRootPagePresenter();
    AsyncProvider<StartPagePresenter> getStartPagePresenter();
    AsyncProvider<EventsPagePresenter> getEventsPagePresenter();

    SailingEventsServiceAsync getSailingEventsService();
}