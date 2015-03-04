package com.sap.sailing.gwt.home.client.place.event2.regatta;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.sap.sailing.gwt.home.client.app.HomePlacesNavigator;
import com.sap.sailing.gwt.home.client.place.event.EventClientFactory;
import com.sap.sailing.gwt.home.client.place.event2.AbstractEventActivity;
import com.sap.sailing.gwt.ui.shared.eventview.EventViewDTO;
import com.sap.sailing.gwt.ui.shared.eventview.EventViewDTO.EventType;

public class EventRegattaActivity extends AbstractEventActivity<AbstractEventRegattaPlace> implements EventRegattaView.Presenter {

    private EventRegattaView currentView = new TabletAndDesktopEventView();

    public EventRegattaActivity(AbstractEventRegattaPlace place, EventClientFactory clientFactory,
            HomePlacesNavigator homePlacesNavigator) {
        super(place, clientFactory, homePlacesNavigator);
    }

    @Override
    public void start(final AcceptsOneWidget panel, final EventBus eventBus) {
        currentView.registerPresenter(this);
        panel.setWidget(currentView);
        currentView.navigateTabsTo(currentPlace);
    }
    
    @Override
    public boolean needsSelectionInHeader() {
        EventViewDTO event = ctx.getEventDTO();
        return (event.getType() == EventType.SERIES_EVENT || event.getType() == EventType.MULTI_REGATTA);
    }
    
    @Override
    public boolean showRegattaMetadata() {
        return ctx.getEventDTO().getType() == EventType.MULTI_REGATTA && ctx.getRegatta() != null;
    }
}
