package com.sap.sailing.gwt.autoplay.client.places.screens.liveraceloop.racemapwithleaderboard;

import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceTokenizer;
import com.sap.sailing.gwt.home.communication.event.sixtyinch.GetSixtyInchStatisticDTO;
import com.sap.sailing.gwt.ui.client.CompetitorSelectionModel;
import com.sap.sailing.gwt.ui.client.shared.racemap.RaceMap;

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
    private CompetitorSelectionModel raceMapSelectionProvider;
    private GetSixtyInchStatisticDTO statistic;

    public void setRaceMap(RaceMap result, CompetitorSelectionModel csel) {
        this.raceMap = result;
        this.raceMapSelectionProvider = csel;
    }
    
    public GetSixtyInchStatisticDTO getStatistic() {
        return statistic;
    }

    public RaceMap getRaceMap() {
        return raceMap;
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

    public void setStatistic(GetSixtyInchStatisticDTO result) {
        statistic = result;
    }

}
