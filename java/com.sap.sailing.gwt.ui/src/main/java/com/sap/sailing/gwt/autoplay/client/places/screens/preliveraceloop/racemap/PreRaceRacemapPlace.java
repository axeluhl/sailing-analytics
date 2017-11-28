package com.sap.sailing.gwt.autoplay.client.places.screens.preliveraceloop.racemap;

import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceTokenizer;
import com.sap.sailing.gwt.ui.client.CompetitorSelectionModel;
import com.sap.sailing.gwt.ui.client.RaceTimesInfoProvider;
import com.sap.sailing.gwt.ui.client.shared.racemap.RaceMap;
import com.sap.sse.gwt.client.player.Timer;

public class PreRaceRacemapPlace extends Place {
    public static class Tokenizer implements PlaceTokenizer<PreRaceRacemapPlace> {
        @Override
        public String getToken(PreRaceRacemapPlace place) {
            return "";
        }

        @Override
        public PreRaceRacemapPlace getPlace(String token) {
            return new PreRaceRacemapPlace();
        }
    }

    private RaceMap raceMap;
    private Throwable error;
    private CompetitorSelectionModel raceMapSelectionProvider;
    private String url;
    private Timer raceboardTimer;
    private RaceTimesInfoProvider timeProvider;

    public void setRaceMap(RaceMap result, CompetitorSelectionModel csel, Timer raceboardTimer, RaceTimesInfoProvider timeProvider) {
        this.raceMap = result;
        this.raceMapSelectionProvider = csel;
        this.raceboardTimer = raceboardTimer;
        this.timeProvider = timeProvider;
    }

    public RaceMap getRaceMap() {
        return raceMap;
    }
    
    public Timer getRaceboardTimer() {
        return raceboardTimer;
    }

    public Throwable getError() {
        return error;
    }

    public void setError(Throwable caught) {
        this.error = caught;
    }

    public CompetitorSelectionModel getRaceMapSelectionProvider() {
        return raceMapSelectionProvider;
    }

    public void setURL(String officialWebsiteURL) {
        this.url = officialWebsiteURL;
    }

    public String getUrl() {
        return url;
    }

    public RaceTimesInfoProvider getTimeProvider() {
        return timeProvider;
    }
}
