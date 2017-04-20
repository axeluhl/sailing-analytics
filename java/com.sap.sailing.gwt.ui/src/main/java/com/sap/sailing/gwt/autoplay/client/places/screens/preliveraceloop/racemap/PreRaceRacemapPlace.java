package com.sap.sailing.gwt.autoplay.client.places.screens.preliveraceloop.racemap;

import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceTokenizer;
import com.sap.sailing.gwt.home.communication.event.minileaderboard.GetMiniLeaderboardDTO;
import com.sap.sailing.gwt.ui.client.CompetitorSelectionModel;
import com.sap.sailing.gwt.ui.client.shared.racemap.RaceMap;

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
    private GetMiniLeaderboardDTO leaderboard;

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

    public CompetitorSelectionModel getRaceMapSelectionProvider() {
        return raceMapSelectionProvider;
    }

    public void setURL(String officialWebsiteURL) {
        this.url = officialWebsiteURL;
    }

    public String getUrl() {
        return url;
    }

    public void setLeaderBoardDTO(GetMiniLeaderboardDTO dto) {
        this.leaderboard = dto;
    }

    public GetMiniLeaderboardDTO getLeaderboard() {
        return leaderboard;
    }

}
