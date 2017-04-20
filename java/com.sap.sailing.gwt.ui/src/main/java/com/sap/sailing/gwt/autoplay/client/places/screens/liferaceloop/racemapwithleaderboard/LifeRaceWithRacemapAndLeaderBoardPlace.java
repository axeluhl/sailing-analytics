package com.sap.sailing.gwt.autoplay.client.places.screens.liferaceloop.racemapwithleaderboard;

import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceTokenizer;
import com.sap.sailing.gwt.home.communication.event.minileaderboard.GetMiniLeaderboardDTO;
import com.sap.sailing.gwt.ui.client.CompetitorSelectionModel;
import com.sap.sailing.gwt.ui.client.shared.racemap.RaceMap;

public class LifeRaceWithRacemapAndLeaderBoardPlace extends Place {
    public static class Tokenizer implements PlaceTokenizer<LifeRaceWithRacemapAndLeaderBoardPlace> {
        @Override
        public String getToken(LifeRaceWithRacemapAndLeaderBoardPlace place) {
            return "";
        }

        @Override
        public LifeRaceWithRacemapAndLeaderBoardPlace getPlace(String token) {
            return new LifeRaceWithRacemapAndLeaderBoardPlace();
        }
    }

    private GetMiniLeaderboardDTO leaderBoardDTO;
    private RaceMap raceMap;
    private Throwable error;
    private CompetitorSelectionModel raceMapSelectionProvider;

    public void setRaceMap(RaceMap result, CompetitorSelectionModel csel) {
        this.raceMap = result;
        this.raceMapSelectionProvider = csel;
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

    public GetMiniLeaderboardDTO getLeaderBoardDTO() {
        return leaderBoardDTO;
    }

    public CompetitorSelectionModel getRaceMapSelectionProvider() {
        return raceMapSelectionProvider;
    }

    public void setLeaderBoardDTO(GetMiniLeaderboardDTO leaderBoardDTO) {
        this.leaderBoardDTO = leaderBoardDTO;
    }

}
