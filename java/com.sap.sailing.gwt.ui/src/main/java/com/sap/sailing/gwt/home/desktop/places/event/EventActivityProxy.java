package com.sap.sailing.gwt.home.desktop.places.event;

import java.util.UUID;

import com.google.gwt.core.client.GWT;
import com.sap.sailing.gwt.home.desktop.app.DesktopPlacesNavigator;
import com.sap.sailing.gwt.home.desktop.places.event.multiregatta.AbstractMultiregattaEventPlace;
import com.sap.sailing.gwt.home.desktop.places.event.multiregatta.EventMultiregattaActivity;
import com.sap.sailing.gwt.home.desktop.places.event.multiregatta.mediatab.MultiregattaMediaPlace;
import com.sap.sailing.gwt.home.desktop.places.event.multiregatta.overviewtab.MultiregattaOverviewPlace;
import com.sap.sailing.gwt.home.desktop.places.event.multiregatta.regattastab.MultiregattaRegattasPlace;
import com.sap.sailing.gwt.home.desktop.places.event.regatta.AbstractEventRegattaPlace;
import com.sap.sailing.gwt.home.desktop.places.event.regatta.EventRegattaActivity;
import com.sap.sailing.gwt.home.desktop.places.event.regatta.leaderboardtab.RegattaLeaderboardPlace;
import com.sap.sailing.gwt.home.desktop.places.event.regatta.mediatab.RegattaMediaPlace;
import com.sap.sailing.gwt.home.desktop.places.event.regatta.overviewtab.RegattaOverviewPlace;
import com.sap.sailing.gwt.home.desktop.places.event.regatta.racestab.RegattaRacesPlace;
import com.sap.sailing.gwt.home.mobile.places.event.latestnews.LatestNewsPlace;
import com.sap.sailing.gwt.home.mobile.places.event.minileaderboard.MiniLeaderboardPlace;
import com.sap.sailing.gwt.home.shared.dispatch.ActivityProxyCallback;
import com.sap.sailing.gwt.home.shared.places.event.AbstractEventPlace;
import com.sap.sailing.gwt.home.shared.places.event.EventContext;
import com.sap.sailing.gwt.home.shared.places.event.EventDefaultPlace;
import com.sap.sailing.gwt.ui.shared.dispatch.event.GetEventViewAction;
import com.sap.sailing.gwt.ui.shared.eventview.EventViewDTO;
import com.sap.sailing.gwt.ui.shared.eventview.EventViewDTO.EventType;
import com.sap.sse.gwt.client.mvp.AbstractActivityProxy;

public class EventActivityProxy extends AbstractActivityProxy {

    private AbstractEventPlace place;
    private EventClientFactory clientFactory;
    private DesktopPlacesNavigator homePlacesNavigator;

    public EventActivityProxy(AbstractEventPlace place, EventClientFactory clientFactory,
            DesktopPlacesNavigator homePlacesNavigator) {
        this.place = place;
        this.homePlacesNavigator = homePlacesNavigator;
        this.clientFactory = clientFactory;
    }

    @Override
    protected void startAsync() {
        GetEventViewAction action = new GetEventViewAction(UUID.fromString(place.getEventUuidAsString()));
        clientFactory.getDispatch().execute(action, new ActivityProxyCallback<EventViewDTO>(clientFactory, place) {
            @Override
            public void onSuccess(EventViewDTO event) {
                afterEventLoad(event);
            }
        });
    }

    private void afterEventLoad(EventViewDTO event) {
        if(place instanceof EventDefaultPlace) {
            place = getRealPlace(event);
        }
        place = verifyAndAdjustPlace(event);
        afterLoad(event);
    }

    private void afterLoad(final EventViewDTO event) {
        GWT.runAsync(new AbstractRunAsyncCallback() {
            @Override
            public void onSuccess() {
                if(place instanceof AbstractEventRegattaPlace) {
                    super.onSuccess(new EventRegattaActivity((AbstractEventRegattaPlace) place, event, clientFactory,
                            homePlacesNavigator));
                }
                if(place instanceof AbstractMultiregattaEventPlace) {
                    super.onSuccess(new EventMultiregattaActivity((AbstractMultiregattaEventPlace) place, event,
                            clientFactory, homePlacesNavigator));
                }
            }
        });
    }

    private AbstractEventPlace getRealPlace(EventViewDTO event) {
        if(event.getType() == EventType.SERIES_EVENT || event.getType() == EventType.SINGLE_REGATTA) {
            return new RegattaOverviewPlace(new EventContext(place.getCtx()).withRegattaId(null));
        }
        return new MultiregattaOverviewPlace(place.getCtx());
    }
    
    /**
     * Checks if the place is valid for the given event.
     * If not, the place is automatically being adjusted.
     * @param event 
     */
    private AbstractEventPlace verifyAndAdjustPlace(EventViewDTO event) {
        EventContext contextWithoutRegatta = new EventContext(place.getCtx()).withRegattaId(null);
        // TODO check if regatta ID is valid
        if(place instanceof AbstractMultiregattaEventPlace && event.getType() != EventType.MULTI_REGATTA) {
            // Events with a type other than multi regatta only have regatta level pages
            if(place instanceof MultiregattaRegattasPlace) {
                return new RegattaRacesPlace(contextWithoutRegatta);
            }
            if(place instanceof MultiregattaMediaPlace) {
                return new RegattaMediaPlace(contextWithoutRegatta);
            }
            return new RegattaOverviewPlace(contextWithoutRegatta);
        }
        
        if(place instanceof AbstractEventRegattaPlace) {
            boolean regattaKnown = event.isRegattaIDKnown(place.getCtx().getRegattaId());
            if(event.getType() != EventType.MULTI_REGATTA && place.getCtx().getRegattaId() != null && !regattaKnown) {
                // Regatta ID unknown but unnecessary ...
                place.getCtx().withRegattaId(null);
            } else if(event.getType() == EventType.MULTI_REGATTA && !regattaKnown) {
                return new MultiregattaRegattasPlace(contextWithoutRegatta);
            }
        }
        
        if(place instanceof RegattaMediaPlace && event.getType() == EventType.MULTI_REGATTA) {
            // The media page for multi regatta events is on event level only but not on regatta level
            return new MultiregattaMediaPlace(new EventContext(place.getCtx()).withRegattaId(null));
        }
        
        if(place instanceof LatestNewsPlace) {
            if(event.getType() == EventType.MULTI_REGATTA) {
                return new MultiregattaOverviewPlace(new EventContext(place.getCtx()).withRegattaId(null));
            }
            return new RegattaOverviewPlace(new EventContext(place.getCtx()).withRegattaId(null));
        }
        
        if(place instanceof MiniLeaderboardPlace) {
            return new RegattaLeaderboardPlace(place.getCtx());
        }
        
        // no adjustment necessary
        return place;
    }
}
