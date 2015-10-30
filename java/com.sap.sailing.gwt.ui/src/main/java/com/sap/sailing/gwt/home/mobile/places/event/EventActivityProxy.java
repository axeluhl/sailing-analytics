package com.sap.sailing.gwt.home.mobile.places.event;

import com.google.gwt.core.client.GWT;
import com.sap.sailing.gwt.home.communication.eventview.EventViewDTO;
import com.sap.sailing.gwt.home.desktop.places.event.multiregatta.mediatab.MultiregattaMediaPlace;
import com.sap.sailing.gwt.home.desktop.places.event.multiregatta.overviewtab.MultiregattaOverviewPlace;
import com.sap.sailing.gwt.home.desktop.places.event.multiregatta.regattastab.MultiregattaRegattasPlace;
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
import com.sap.sailing.gwt.home.shared.places.event.AbstractEventActivityProxy;
import com.sap.sailing.gwt.home.shared.places.event.AbstractEventPlace;

public class EventActivityProxy extends AbstractEventActivityProxy<MobileApplicationClientFactory> {

    public EventActivityProxy(AbstractEventPlace place, MobileApplicationClientFactory clientFactory) {
        super(clientFactory, place);
    }
    
    @Override
    protected void afterEventLoad(final MobileApplicationClientFactory clientFactory, final EventViewDTO event,
            final AbstractEventPlace place) {
        GWT.runAsync(new AbstractRunAsyncCallback() {
            @Override
            public void onSuccess() {
                if (place instanceof MultiregattaOverviewPlace || place instanceof MultiregattaRegattasPlace) {
                    super.onSuccess(new MultiRegattaActivity(place, event, getNavigationPathDisplay(), clientFactory));
                } else if (place instanceof RegattaOverviewPlace) {
                    super.onSuccess(new RegattaActivity((RegattaOverviewPlace) place, event, getNavigationPathDisplay(), clientFactory));
                } else if (place instanceof RegattaRacesPlace) {
                    super.onSuccess(new RacesActivity((RegattaRacesPlace) place, event, getNavigationPathDisplay(), clientFactory));
                } else if (place instanceof MiniLeaderboardPlace) {
                    super.onSuccess(new MiniLeaderboardActivity((MiniLeaderboardPlace) place, event, getNavigationPathDisplay(), clientFactory));
                } else if (place instanceof LatestNewsPlace) {
                    super.onSuccess(new LatestNewsActivity((LatestNewsPlace) place, event, clientFactory));
                } else if (place instanceof RegattaMediaPlace || place instanceof MultiregattaMediaPlace) {
                    super.onSuccess(new MediaActivity(place, event, clientFactory));
                }
            }
        });
    }
}
