package com.sap.sailing.gwt.home.mobile.places.event;

import java.util.UUID;

import com.google.gwt.core.client.GWT;
import com.sap.sailing.gwt.home.communication.event.GetEventViewAction;
import com.sap.sailing.gwt.home.communication.eventview.EventViewDTO;
import com.sap.sailing.gwt.home.communication.eventview.EventViewDTO.EventType;
import com.sap.sailing.gwt.home.desktop.places.event.multiregatta.mediatab.MultiregattaMediaPlace;
import com.sap.sailing.gwt.home.desktop.places.event.multiregatta.overviewtab.MultiregattaOverviewPlace;
import com.sap.sailing.gwt.home.desktop.places.event.regatta.mediatab.RegattaMediaPlace;
import com.sap.sailing.gwt.home.desktop.places.event.regatta.overviewtab.RegattaOverviewPlace;
import com.sap.sailing.gwt.home.desktop.places.event.regatta.racestab.RegattaRacesPlace;
import com.sap.sailing.gwt.home.mobile.app.MobileApplicationClientFactory;
import com.sap.sailing.gwt.home.mobile.places.event.latestnews.LatestNewsActivity;
import com.sap.sailing.gwt.home.mobile.places.event.latestnews.LatestNewsPlace;
import com.sap.sailing.gwt.home.mobile.places.event.media.MediaActivity;
import com.sap.sailing.gwt.home.mobile.places.event.minileaderboard.MiniLeaderboardActivity;
import com.sap.sailing.gwt.home.mobile.places.event.minileaderboard.MiniLeaderboardPlace;
import com.sap.sailing.gwt.home.mobile.places.event.overview.multiregatta.MultiRegattaActivity;
import com.sap.sailing.gwt.home.mobile.places.event.overview.regatta.RegattaActivity;
import com.sap.sailing.gwt.home.mobile.places.event.races.RacesActivity;
import com.sap.sailing.gwt.home.shared.app.ActivityProxyCallback;
import com.sap.sailing.gwt.home.shared.places.event.AbstractEventPlace;
import com.sap.sailing.gwt.home.shared.places.event.EventContext;
import com.sap.sailing.gwt.home.shared.places.event.EventDefaultPlace;
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
        GetEventViewAction action = new GetEventViewAction(UUID.fromString(currentPlace.getEventUuidAsString()));
        clientFactory.getDispatch().execute(action, new ActivityProxyCallback<EventViewDTO>(clientFactory, currentPlace) {
            @Override
            public void onSuccess(EventViewDTO event) {
                if (currentPlace instanceof EventDefaultPlace) {
                    currentPlace = getRealPlace(event.getType());
                }
                afterEventLoad(event);
            }
        });
    }
    
    private AbstractEventPlace getRealPlace(EventType eventType) {
        if(eventType == EventType.SERIES_EVENT || eventType == EventType.SINGLE_REGATTA) {
            return new RegattaOverviewPlace(new EventContext(currentPlace.getCtx()).withRegattaId(null));
        }
        return new MultiregattaOverviewPlace(currentPlace.getCtx());
    }
    
    private void afterEventLoad(final EventViewDTO event) {
        GWT.runAsync(new AbstractRunAsyncCallback() {
            @Override
            public void onSuccess() {
                if (currentPlace instanceof MultiregattaOverviewPlace) {
                    super.onSuccess(new MultiRegattaActivity(currentPlace, event, clientFactory));
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
