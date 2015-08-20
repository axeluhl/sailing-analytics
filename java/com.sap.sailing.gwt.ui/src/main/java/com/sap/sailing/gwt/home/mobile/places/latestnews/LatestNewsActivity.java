package com.sap.sailing.gwt.home.mobile.places.latestnews;

import com.google.gwt.user.client.Window;
import com.sap.sailing.gwt.home.mobile.app.MobileApplicationClientFactory;
import com.sap.sailing.gwt.home.mobile.places.event.AbstractEventActivity;
import com.sap.sailing.gwt.home.mobile.places.event.EventViewBase;
import com.sap.sailing.gwt.home.mobile.places.latestnews.LatestNewsView.Presenter;

public class LatestNewsActivity extends AbstractEventActivity<LatestNewsPlace> implements Presenter {

    public LatestNewsActivity(LatestNewsPlace place, MobileApplicationClientFactory clientFactory) {
        super(place, clientFactory);
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
