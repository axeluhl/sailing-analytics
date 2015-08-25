package com.sap.sailing.gwt.home.mobile.places.event;

import java.util.UUID;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sailing.gwt.home.client.place.error.ErrorPlace;
import com.sap.sailing.gwt.home.client.place.event.AbstractEventPlace;
import com.sap.sailing.gwt.home.client.place.event.EventDefaultPlace;
import com.sap.sailing.gwt.home.client.place.event.regatta.tabs.RegattaOverviewPlace;
import com.sap.sailing.gwt.home.client.place.event.regatta.tabs.RegattaRacesPlace;
import com.sap.sailing.gwt.home.mobile.app.MobileApplicationClientFactory;
import com.sap.sailing.gwt.home.mobile.places.event.minileaderboard.MiniLeaderboardActivity;
import com.sap.sailing.gwt.home.mobile.places.event.minileaderboard.MiniLeaderboardPlace;
import com.sap.sailing.gwt.home.mobile.places.event.overview.EventActivity;
import com.sap.sailing.gwt.home.mobile.places.event.races.RacesActivity;
import com.sap.sailing.gwt.home.mobile.places.latestnews.LatestNewsActivity;
import com.sap.sailing.gwt.home.mobile.places.latestnews.LatestNewsPlace;
import com.sap.sailing.gwt.home.mobile.places.regatta.RegattaActivity;
import com.sap.sailing.gwt.ui.shared.dispatch.event.GetEventViewAction;
import com.sap.sailing.gwt.ui.shared.eventview.EventViewDTO;
import com.sap.sse.gwt.client.mvp.AbstractActivityProxy;

public class EventActivityProxy extends AbstractActivityProxy {

    private final MobileApplicationClientFactory clientFactory;
    private final AbstractEventPlace currentPlace;

    public EventActivityProxy(AbstractEventPlace place, MobileApplicationClientFactory clientFactory) {
        this.currentPlace = place;
        this.clientFactory = clientFactory;
    }

    @Override
    protected void startAsync() {
        if(currentPlace.getCtx().getEventDTO() != null) {
            afterEventLoad();
            return;
        }
        UUID eventId = UUID.fromString(currentPlace.getCtx().getEventId());
        clientFactory.getDispatch().execute(new GetEventViewAction(eventId), new AsyncCallback<EventViewDTO>() {
            @Override
            public void onSuccess(final EventViewDTO event) {
                currentPlace.getCtx().updateContext(event);
                afterEventLoad();
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
    
    private void afterEventLoad() {
        GWT.runAsync(new AbstractRunAsyncCallback() {
            @Override
            public void onSuccess() {
                if (currentPlace instanceof EventDefaultPlace) {
                    super.onSuccess(new EventActivity(currentPlace, clientFactory));
                } else if (currentPlace instanceof RegattaOverviewPlace) {
                    super.onSuccess(new RegattaActivity((RegattaOverviewPlace) currentPlace, clientFactory));
                } else if (currentPlace instanceof RegattaRacesPlace) {
                    super.onSuccess(new RacesActivity((RegattaRacesPlace) currentPlace, clientFactory));
                } else if (currentPlace instanceof MiniLeaderboardPlace) {
                    super.onSuccess(new MiniLeaderboardActivity((MiniLeaderboardPlace) currentPlace, clientFactory));
                } else if (currentPlace instanceof LatestNewsPlace) {
                    super.onSuccess(new LatestNewsActivity((LatestNewsPlace) currentPlace, clientFactory));
                }
            }
        });
    }
}
