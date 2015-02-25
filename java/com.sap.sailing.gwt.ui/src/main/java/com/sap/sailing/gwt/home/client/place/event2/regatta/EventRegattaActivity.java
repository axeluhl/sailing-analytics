package com.sap.sailing.gwt.home.client.place.event2.regatta;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.sap.sailing.gwt.home.client.place.event.EventClientFactory;
import com.sap.sailing.gwt.home.client.place.event2.EventActivity;
import com.sap.sailing.gwt.home.client.place.event2.model.EventDTO;
import com.sap.sailing.gwt.home.client.place.event2.model.EventType;

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
    
    @Override
    public boolean needsSelectionInHeader() {
        EventDTO event = ctx.getEventDTO();
        return (event.getType() == EventType.SERIES_EVENT || event.getType() == EventType.MULTI_REGATTA);
    }
    
    @Override
    public String getEventName() {
        if(ctx.getEventDTO().getType() == EventType.MULTI_REGATTA) {
            return ctx.getRegattaId();
        }
        return super.getEventName();
    }
}
