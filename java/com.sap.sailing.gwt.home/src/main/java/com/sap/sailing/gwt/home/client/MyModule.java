package com.sap.sailing.gwt.home.client;

import com.gwtplatform.mvp.client.gin.AbstractPresenterModule;
import com.gwtplatform.mvp.client.gin.DefaultModule;
import com.sap.sailing.gwt.home.client.aboutuspage.AboutUsPagePresenter;
import com.sap.sailing.gwt.home.client.aboutuspage.AboutUsPageView;
import com.sap.sailing.gwt.home.client.contactpage.ContactPagePresenter;
import com.sap.sailing.gwt.home.client.contactpage.ContactPageView;
import com.sap.sailing.gwt.home.client.eventspage.EventsPagePresenter;
import com.sap.sailing.gwt.home.client.eventspage.EventsPageView;
import com.sap.sailing.gwt.home.client.shared.PageNameTokens;
import com.sap.sailing.gwt.home.client.startpage.StartPagePresenter;
import com.sap.sailing.gwt.home.client.startpage.StartPageView;

public class MyModule extends AbstractPresenterModule {
    @Override
    protected void configure() {
        install(new DefaultModule(MyPlaceManager.class));

        bindConstant().annotatedWith(DefaultPlace.class).to(PageNameTokens.startPage);

        // Presenters
        bindPresenter(RootPagePresenter.class, RootPagePresenter.MyView.class, RootPageView.class, RootPagePresenter.MyProxy.class);

        bindPresenter(StartPagePresenter.class, StartPagePresenter.MyView.class, StartPageView.class,
        		StartPagePresenter.MyProxy.class);
        bindPresenter(EventsPagePresenter.class, EventsPagePresenter.MyView.class, EventsPageView.class,
        		EventsPagePresenter.MyProxy.class);
        bindPresenter(AboutUsPagePresenter.class, AboutUsPagePresenter.MyView.class, AboutUsPageView.class,
        		AboutUsPagePresenter.MyProxy.class);
        bindPresenter(ContactPagePresenter.class, ContactPagePresenter.MyView.class, ContactPageView.class,
        		ContactPagePresenter.MyProxy.class);
    }
}