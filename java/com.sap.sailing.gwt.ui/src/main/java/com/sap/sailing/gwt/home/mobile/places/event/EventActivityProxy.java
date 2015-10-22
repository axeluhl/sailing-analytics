package com.sap.sailing.gwt.home.mobile.places.event;

import java.util.UUID;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sailing.gwt.home.desktop.places.error.ErrorPlace;
import com.sap.sailing.gwt.home.desktop.places.event.multiregatta.mediatab.MultiregattaMediaPlace;
import com.sap.sailing.gwt.home.desktop.places.event.regatta.mediatab.RegattaMediaPlace;
import com.sap.sailing.gwt.home.desktop.places.event.regatta.overviewtab.RegattaOverviewPlace;
import com.sap.sailing.gwt.home.desktop.places.event.regatta.racestab.RegattaRacesPlace;
import com.sap.sailing.gwt.home.mobile.app.MobileApplicationClientFactory;
import com.sap.sailing.gwt.home.mobile.places.event.latestnews.LatestNewsActivity;
import com.sap.sailing.gwt.home.mobile.places.event.latestnews.LatestNewsPlace;
import com.sap.sailing.gwt.home.mobile.places.event.media.MediaActivity;
import com.sap.sailing.gwt.home.mobile.places.event.minileaderboard.MiniLeaderboardActivity;
import com.sap.sailing.gwt.home.mobile.places.event.minileaderboard.MiniLeaderboardPlace;
import com.sap.sailing.gwt.home.mobile.places.event.overview.EventActivity;
import com.sap.sailing.gwt.home.mobile.places.event.races.RacesActivity;
import com.sap.sailing.gwt.home.mobile.places.event.regatta.RegattaActivity;
import com.sap.sailing.gwt.home.shared.places.event.AbstractEventPlace;
import com.sap.sailing.gwt.home.shared.places.event.EventContext;
import com.sap.sailing.gwt.home.shared.places.event.EventDefaultPlace;
import com.sap.sailing.gwt.ui.shared.dispatch.event.GetEventViewAction;
import com.sap.sailing.gwt.ui.shared.eventview.EventViewDTO;
import com.sap.sailing.gwt.ui.shared.eventview.EventViewDTO.EventType;
import com.sap.sse.gwt.client.mvp.AbstractActivityProxy;

public class EventActivityProxy extends AbstractActivityProxy {

    private final MobileApplicationClientFactory clientFactory;
    private AbstractEventPlace currentPlace;

    public EventActivityProxy(AbstractEventPlace place, MobileApplicationClientFactory clientFactory) {
        this.currentPlace = place;
        this.clientFactory = clientFactory;
    }

    @Override
    protected void startAsync() {
        UUID eventId = UUID.fromString(currentPlace.getCtx().getEventId());
        clientFactory.getDispatch().execute(new GetEventViewAction(eventId), new AsyncCallback<EventViewDTO>() {
            @Override
            public void onSuccess(final EventViewDTO event) {
                currentPlace = getRealPlace(event.getType());
                afterEventLoad(event);
            }
            
            @Override
            public void onFailure(Throwable caught) {
                // TODO @FM: extract text?
                ErrorPlace errorPlace = new ErrorPlace("Error while loading the event with service getEventViewById()");
                // TODO @FM: reload sinnvoll hier?
                errorPlace.setComingFrom(currentPlace);
                clientFactory.getPlaceController().goTo(errorPlace);
            }
        });
    }
    
    private AbstractEventPlace getRealPlace(EventType eventType) {
        if(currentPlace instanceof RegattaOverviewPlace && (eventType == EventType.SERIES_EVENT || eventType == EventType.SINGLE_REGATTA)) {
            EventContext contextWithoutRegatta = new EventContext(currentPlace.getCtx()).withRegattaId(null);
            return new EventDefaultPlace(contextWithoutRegatta);
        }
        return currentPlace;
    }
    
    private void afterEventLoad(final EventViewDTO event) {
        GWT.runAsync(new AbstractRunAsyncCallback() {
            @Override
            public void onSuccess() {
                if (currentPlace instanceof EventDefaultPlace) {
                    super.onSuccess(new EventActivity(currentPlace, event, clientFactory));
                } else if (currentPlace instanceof RegattaOverviewPlace) {
                    super.onSuccess(new RegattaActivity((RegattaOverviewPlace) currentPlace, event, clientFactory));
                } else if (currentPlace instanceof RegattaRacesPlace) {
                    super.onSuccess(new RacesActivity((RegattaRacesPlace) currentPlace, event, clientFactory));
                } else if (currentPlace instanceof MiniLeaderboardPlace) {
                    super.onSuccess(new MiniLeaderboardActivity((MiniLeaderboardPlace) currentPlace, event, clientFactory));
                } else if (currentPlace instanceof LatestNewsPlace) {
                    super.onSuccess(new LatestNewsActivity((LatestNewsPlace) currentPlace, event, clientFactory));
                } else if (currentPlace instanceof RegattaMediaPlace || currentPlace instanceof MultiregattaMediaPlace) {
                    super.onSuccess(new MediaActivity(currentPlace, event, clientFactory));
                }
            }
        });
    }
}
