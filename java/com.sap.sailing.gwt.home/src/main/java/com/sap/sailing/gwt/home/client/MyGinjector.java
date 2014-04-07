package com.sap.sailing.gwt.home.client;

import com.google.gwt.inject.client.AsyncProvider;
import com.google.gwt.inject.client.GinModules;
import com.google.gwt.inject.client.Ginjector;
import com.google.inject.Provider;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.sap.sailing.gwt.home.client.aboutuspage.AboutUsPagePresenter;
import com.sap.sailing.gwt.home.client.contactpage.ContactPagePresenter;
import com.sap.sailing.gwt.home.client.eventspage.EventsPagePresenter;
import com.sap.sailing.gwt.home.client.startpage.StartPagePresenter;

@GinModules({ MyModule.class })
public interface MyGinjector extends Ginjector {
    PlaceManager getPlaceManager();

    EventBus getEventBus();

    Provider<RootPagePresenter> getRootPagePresenter();
    AsyncProvider<StartPagePresenter> getStartPagePresenter();
    AsyncProvider<EventsPagePresenter> getEventsPagePresenter();
    AsyncProvider<AboutUsPagePresenter> getAboutUsPagePresenter();
    AsyncProvider<ContactPagePresenter> getContactPagePresenter();

    SailingEventsServiceAsync getSailingEventsService();
}