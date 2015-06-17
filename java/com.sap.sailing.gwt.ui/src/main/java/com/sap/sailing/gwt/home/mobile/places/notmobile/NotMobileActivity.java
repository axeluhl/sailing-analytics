package com.sap.sailing.gwt.home.mobile.places.notmobile;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.sap.sailing.gwt.home.mobile.app.MobileApplicationClientFactory;
import com.sap.sailing.gwt.home.mobile.places.notmobile.NotMobileView.Presenter;
import com.sap.sailing.gwt.home.shared.app.ApplicationHistoryMapper;

public class NotMobileActivity extends AbstractActivity implements Presenter {
    private final MobileApplicationClientFactory clientFactory;
    private final Place notMobilePlace;
    private final ApplicationHistoryMapper mapper = GWT.create(ApplicationHistoryMapper.class);
    private final Place comingFrom;

    public NotMobileActivity(Place comingFrom, Place notMobilePlace, MobileApplicationClientFactory clientFactory) {
        this.comingFrom = comingFrom;
        this.clientFactory = clientFactory;
        this.notMobilePlace = notMobilePlace;
    }

    @Override
    public void start(final AcceptsOneWidget panel, EventBus eventBus) {
        final NotMobileView view = new NotMobileViewImpl(this);
        view.setGotoDesktopUrl("HomeDesktop.html" + Window.Location.getQueryString() + "#"
                + mapper.getToken(notMobilePlace));
        panel.setWidget(view.asWidget());
    }


    @Override
    public void goBack() {
        clientFactory.getPlaceController().goTo(comingFrom);
    }

}
