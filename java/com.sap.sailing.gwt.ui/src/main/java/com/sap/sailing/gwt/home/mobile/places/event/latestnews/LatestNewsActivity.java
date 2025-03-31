package com.sap.sailing.gwt.home.mobile.places.event.latestnews;

import java.util.List;

import com.google.gwt.user.client.Window;
import com.sap.sailing.gwt.home.communication.eventview.EventViewDTO;
import com.sap.sailing.gwt.home.mobile.app.MobileApplicationClientFactory;
import com.sap.sailing.gwt.home.mobile.places.event.AbstractEventActivity;
import com.sap.sailing.gwt.home.mobile.places.event.EventViewBase;
import com.sap.sailing.gwt.home.mobile.places.event.latestnews.LatestNewsView.Presenter;
import com.sap.sailing.gwt.home.shared.app.NavigationPathDisplay;
import com.sap.sailing.gwt.home.shared.app.NavigationPathDisplay.NavigationItem;
import com.sap.sailing.gwt.ui.client.StringMessages;

public class LatestNewsActivity extends AbstractEventActivity<LatestNewsPlace> implements Presenter {

    public LatestNewsActivity(LatestNewsPlace place, EventViewDTO eventDTO, NavigationPathDisplay navigationPathDisplay, MobileApplicationClientFactory clientFactory) {
        super(place, eventDTO, clientFactory);
        initNavigationPath(navigationPathDisplay);
    }
    
    private void initNavigationPath(NavigationPathDisplay navigationPathDisplay) {
        StringMessages i18n = StringMessages.INSTANCE;
        List<NavigationItem> navigationItems = getNavigationPathToEventLevel();
        navigationItems.add(new NavigationItem(i18n.latestNews(), getLatesNewsNavigation()));
        navigationPathDisplay.showNavigationPath(navigationItems.toArray(new NavigationItem[navigationItems.size()]));
    }

    @Override
    protected EventViewBase initView() {
        final LatestNewsView view = new LatestNewsViewImpl(this);
        Window.setTitle(getPlace().getTitle());
        initQuickfinder(view, false);
        view.showNews(getPlace().getNews());
        return view;
    }

    @Override
    protected boolean isRegattaLevel() {
        return !isMultiRegattaEvent();
    }
}
