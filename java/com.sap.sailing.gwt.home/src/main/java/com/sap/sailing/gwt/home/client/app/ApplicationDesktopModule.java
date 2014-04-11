package com.sap.sailing.gwt.home.client.app;

import javax.inject.Singleton;

import com.gwtplatform.mvp.client.gin.AbstractPresenterModule;
import com.sap.sailing.gwt.home.client.aboutuspage.AboutUsPagePresenter;
import com.sap.sailing.gwt.home.client.aboutuspage.AboutUsPageView;
import com.sap.sailing.gwt.home.client.contactpage.ContactPagePresenter;
import com.sap.sailing.gwt.home.client.contactpage.ContactPageView;
import com.sap.sailing.gwt.home.client.eventpage.EventPagePresenter;
import com.sap.sailing.gwt.home.client.eventpage.EventPageView;
import com.sap.sailing.gwt.home.client.eventspage.EventsPagePresenter;
import com.sap.sailing.gwt.home.client.eventspage.EventsPageView;
import com.sap.sailing.gwt.home.client.startpage.StartPagePresenter;
import com.sap.sailing.gwt.home.client.startpage.StartPageView;

public class ApplicationDesktopModule extends AbstractPresenterModule {
    @Override
    protected void configure() {
        // root page presenters
        bind(ApplicationDesktopPresenter.class).in(Singleton.class);
        bind(ApplicationDesktopView.class).in(Singleton.class);
        bind(AbstractRootPagePresenter.MyProxy.class).asEagerSingleton();
        bind(AbstractRootPagePresenter.MyView.class).to(ApplicationDesktopView.class);
        bind(AbstractRootPagePresenter.class).to(ApplicationDesktopPresenter.class);

        // Presenters
        bindPresenter(StartPagePresenter.class, StartPagePresenter.MyView.class, StartPageView.class,
        		StartPagePresenter.MyProxy.class);
        bindPresenter(EventsPagePresenter.class, EventsPagePresenter.MyView.class, EventsPageView.class,
        		EventsPagePresenter.MyProxy.class);
        bindPresenter(EventPagePresenter.class, EventPagePresenter.MyView.class, EventPageView.class,
        		EventPagePresenter.MyProxy.class);
        bindPresenter(AboutUsPagePresenter.class, AboutUsPagePresenter.MyView.class, AboutUsPageView.class,
        		AboutUsPagePresenter.MyProxy.class);
        bindPresenter(ContactPagePresenter.class, ContactPagePresenter.MyView.class, ContactPageView.class,
        		ContactPagePresenter.MyProxy.class);
    }
}
