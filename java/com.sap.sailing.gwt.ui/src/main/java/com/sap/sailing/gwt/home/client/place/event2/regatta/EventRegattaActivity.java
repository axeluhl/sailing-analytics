package com.sap.sailing.gwt.home.client.place.event2.regatta;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.sap.sailing.gwt.home.client.app.HomePlacesNavigator;
import com.sap.sailing.gwt.home.client.app.PlaceNavigation;
import com.sap.sailing.gwt.home.client.place.event.EventClientFactory;
import com.sap.sailing.gwt.home.client.place.event2.AbstractEventActivity;
import com.sap.sailing.gwt.home.client.place.event2.EventContext;
import com.sap.sailing.gwt.home.client.place.event2.EventView.PlaceCallback;
import com.sap.sailing.gwt.home.client.place.event2.regatta.tabs.RegattaRacesPlace;
import com.sap.sailing.gwt.ui.shared.eventview.EventReferenceDTO;
import com.sap.sailing.gwt.ui.shared.eventview.EventViewDTO;
import com.sap.sailing.gwt.ui.shared.eventview.EventViewDTO.EventType;
import com.sap.sailing.gwt.ui.shared.eventview.RegattaReferenceDTO;
import com.sap.sse.gwt.client.ErrorReporter;

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
    public void forPlaceSelection(PlaceCallback callback) {
        EventViewDTO event = ctx.getEventDTO();
        if (event.getType() == EventType.SERIES_EVENT) {
            for(EventReferenceDTO seriesEvent : event.getEventsOfSeries()) {
                AbstractEventRegattaPlace place = currentPlace.newInstanceWithContext(new EventContext().withId(seriesEvent.getId().toString()));
                callback.forPlace(place, seriesEvent.getDisplayName(), (event.id.equals(seriesEvent.getId())));
            }
        } else {
            for(RegattaReferenceDTO regatta : event.getRegattas()) {
                AbstractEventRegattaPlace place = currentPlace.newInstanceWithContext(contextForRegatta(regatta.getId()));
                callback.forPlace(place, regatta.getDisplayName(), (ctx.getRegattaId().equals(regatta.getId())));
            }
        }
    }
    
    @Override
    public boolean showRegattaMetadata() {
        return ctx.getEventDTO().getType() == EventType.MULTI_REGATTA && ctx.getRegatta() != null;
    }
    
    @Override
    public PlaceNavigation<RegattaRacesPlace> getCurrentRegattaOverviewNavigation() {
        // TODO Overview isn't implemented yet
        return homePlacesNavigator.getEventNavigation(new RegattaRacesPlace(ctx), new RegattaRacesPlace.Tokenizer(), ctx.getEventDTO().getBaseURL(), ctx.getEventDTO().isOnRemoteServer());
    }

    @Override
    public ErrorReporter getErrorReporter() {
        return clientFactory.getErrorReporter();
    }
}
