package com.sap.sailing.gwt.autoplay.client.places.screens.liveraceloop.racemapwithleaderboard;

import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceTokenizer;
import com.sap.sailing.gwt.home.communication.event.sixtyinch.GetSixtyInchStatisticDTO;
import com.sap.sailing.gwt.ui.client.RaceCompetitorSelectionModel;
import com.sap.sailing.gwt.ui.client.RaceTimesInfoProvider;
import com.sap.sailing.gwt.ui.client.shared.racemap.RaceMap;
import com.sap.sse.gwt.client.player.Timer;

public class LiveRaceWithRacemapAndLeaderBoardPlace extends Place {
    public static class Tokenizer implements PlaceTokenizer<LiveRaceWithRacemapAndLeaderBoardPlace> {
        @Override
        public String getToken(LiveRaceWithRacemapAndLeaderBoardPlace place) {
            return "";
        }

        @Override
        public LiveRaceWithRacemapAndLeaderBoardPlace getPlace(String token) {
            return new LiveRaceWithRacemapAndLeaderBoardPlace();
        }
    }

    private RaceMap raceMap;
    private Throwable error;
    private RaceCompetitorSelectionModel raceMapSelectionProvider;
    private GetSixtyInchStatisticDTO statistic;
    private Timer raceboardTimer;
    private RaceTimesInfoProvider timeProvider;

    public void setRaceMap(RaceMap result, RaceCompetitorSelectionModel csel, Timer raceboardTimer, RaceTimesInfoProvider timeProvider) {
        this.raceMap = result;
        this.raceMapSelectionProvider = csel;
        this.raceboardTimer = raceboardTimer;
        this.timeProvider = timeProvider;
    }
    
    public GetSixtyInchStatisticDTO getStatistic() {
        return statistic;
    }
    
    public Timer getRaceboardTimer() {
        return raceboardTimer;
    }

    public RaceMap getRaceMap() {
        return raceMap;
    }
    
    public RaceTimesInfoProvider getTimeProvider() {
        return timeProvider;
    }

    public Throwable getError() {
        return error;
    }

    public void setError(Throwable caught) {
        this.error = caught;
    }

    public RaceCompetitorSelectionModel getRaceMapSelectionProvider() {
        return raceMapSelectionProvider;
    }

    public void setStatistic(GetSixtyInchStatisticDTO result) {
        statistic = result;
    }

}
