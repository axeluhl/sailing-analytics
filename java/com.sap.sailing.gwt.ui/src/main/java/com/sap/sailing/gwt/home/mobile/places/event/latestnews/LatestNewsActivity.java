package com.sap.sailing.gwt.home.mobile.places.event.latestnews;

import com.google.gwt.user.client.Window;
import com.sap.sailing.gwt.home.communication.eventview.EventViewDTO;
import com.sap.sailing.gwt.home.mobile.app.MobileApplicationClientFactory;
import com.sap.sailing.gwt.home.mobile.places.event.AbstractEventActivity;
import com.sap.sailing.gwt.home.mobile.places.event.EventViewBase;
import com.sap.sailing.gwt.home.mobile.places.event.latestnews.LatestNewsView.Presenter;

public class LatestNewsActivity extends AbstractEventActivity<LatestNewsPlace> implements Presenter {

    public LatestNewsActivity(LatestNewsPlace place, EventViewDTO eventDTO, MobileApplicationClientFactory clientFactory) {
        super(place, eventDTO, clientFactory);
    }

    @Override
    protected EventViewBase initView() {
        final LatestNewsView view = new LatestNewsViewImpl(this);
        Window.setTitle(getPlace().getTitle());
        initQuickfinder(view, false);
        view.showNews(getPlace().getNews());
        return view;
    }

}
