package com.sap.sailing.gwt.home.client.app;

import javax.inject.Singleton;

import com.gwtplatform.mvp.client.gin.AbstractPresenterModule;
import com.sap.sailing.gwt.home.client.app.aboutus.AboutUsPagePresenter;
import com.sap.sailing.gwt.home.client.app.aboutus.AboutUsPageView;
import com.sap.sailing.gwt.home.client.app.contact.ContactPagePresenter;
import com.sap.sailing.gwt.home.client.app.contact.ContactPageView;
import com.sap.sailing.gwt.home.client.app.event.EventPagePresenter;
import com.sap.sailing.gwt.home.client.app.event.EventPageView;
import com.sap.sailing.gwt.home.client.app.events.EventsPagePresenter;
import com.sap.sailing.gwt.home.client.app.events.EventsPageView;
import com.sap.sailing.gwt.home.client.app.start.StartPagePresenter;
import com.sap.sailing.gwt.home.client.app.start.StartPageTabletView;

public class ApplicationTabletModule extends AbstractPresenterModule {
    @Override
    protected void configure() {
        // Application Presenters
        bind(ApplicationTabletPresenter.class).in(Singleton.class);
        bind(ApplicationTabletView.class).in(Singleton.class);
        bind(AbstractRootPagePresenter.MyProxy.class).asEagerSingleton();
        bind(AbstractRootPagePresenter.MyView.class).to(ApplicationTabletView.class);
        bind(AbstractRootPagePresenter.class).to(ApplicationTabletPresenter.class);

        // Presenters
        bindPresenter(StartPagePresenter.class, StartPagePresenter.MyView.class, StartPageTabletView.class,
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
