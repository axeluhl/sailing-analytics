package com.sap.sailing.gwt.home.client.place.event2.regatta;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.sap.sailing.gwt.home.client.place.event.EventClientFactory;
import com.sap.sailing.gwt.home.client.place.event2.EventActivity;

public class EventRegattaActivity extends EventActivity<AbstractEventRegattaPlace> implements EventRegattaView.Presenter {

    private EventRegattaView currentView = new TabletAndDesktopEventView();

    public EventRegattaActivity(AbstractEventRegattaPlace place, EventClientFactory clientFactory) {
        super(place, clientFactory);
    }

    @Override
    public void start(final AcceptsOneWidget panel, final EventBus eventBus) {
        currentView.registerPresenter(this);
        panel.setWidget(currentView);
        currentView.navigateTabsTo(currentPlace);
    }
}
