package com.sap.sailing.gwt.home.client.place.event2.multiregatta;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.sap.sailing.gwt.home.client.place.event.EventClientFactory;
import com.sap.sailing.gwt.home.client.place.event2.AbstractEventActivity;

public class EventMultiregattaActivity extends AbstractEventActivity<AbstractMultiregattaEventPlace> implements EventMultiregattaView.Presenter {

    private EventMultiregattaView currentView = new TabletAndDesktopEventView();

    public EventMultiregattaActivity(AbstractMultiregattaEventPlace place, EventClientFactory clientFactory) {
        super(place, clientFactory);
    }

    @Override
    public void start(final AcceptsOneWidget panel, final EventBus eventBus) {
        currentView.registerPresenter(this);
        panel.setWidget(currentView);
        currentView.navigateTabsTo(currentPlace);
    }

    
    @Override
    public boolean needsSelectionInHeader() {
        return false;
    }
}
