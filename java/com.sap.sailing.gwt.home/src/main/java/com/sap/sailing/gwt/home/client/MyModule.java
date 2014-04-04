package com.sap.sailing.gwt.home.client;

import com.gwtplatform.mvp.client.gin.AbstractPresenterModule;
import com.gwtplatform.mvp.client.gin.DefaultModule;

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
    }
}