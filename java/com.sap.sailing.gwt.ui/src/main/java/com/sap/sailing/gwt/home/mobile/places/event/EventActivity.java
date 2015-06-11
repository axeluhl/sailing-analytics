package com.sap.sailing.gwt.home.mobile.places.event;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.sap.sailing.gwt.home.client.place.event.AbstractEventPlace;
import com.sap.sailing.gwt.home.mobile.app.MobileApplicationClientFactory;
import com.sap.sailing.gwt.home.mobile.places.event.EventView.Presenter;

public class EventActivity extends AbstractActivity implements
 Presenter {
    private final MobileApplicationClientFactory clientFactory;
    private final AbstractEventPlace place;

    public EventActivity(AbstractEventPlace place, MobileApplicationClientFactory clientFactory) {
        this.clientFactory = clientFactory;
        this.place = place;
    }

    @Override
    public void start(final AcceptsOneWidget panel, EventBus eventBus) {
        final EventView view = new EventViewImpl(this);
        panel.setWidget(view.asWidget());

    }


}
