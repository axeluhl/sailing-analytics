package com.sap.sailing.gwt.home.mobile.places.start;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.sap.sailing.gwt.home.client.place.events.EventsPlace;
import com.sap.sailing.gwt.home.client.place.start.StartPlace;
import com.sap.sailing.gwt.home.mobile.app.ApplicationContext;
import com.sap.sailing.gwt.home.mobile.places.start.StartView.Presenter;

public class StartActivity extends AbstractActivity implements Presenter {
    private final ApplicationContext clientFactory;
    private final StartPlace place;

    public StartActivity(StartPlace place, ApplicationContext clientFactory) {
        this.clientFactory = clientFactory;
        this.place = place;
    }

    @Override
    public void start(final AcceptsOneWidget panel, EventBus eventBus) {
        final StartView view = new StartViewImpl(this);
        panel.setWidget(view.asWidget());
        Window.setTitle(place.getTitle());
    }

    @Override
    public void gotoEvents() {
        clientFactory.goToPlace(new EventsPlace());
    }
}
