package com.sap.sailing.gwt.autoplay.client.place.sixtyinch.slides.slide7;

import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceTokenizer;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.gwt.home.communication.event.minileaderboard.GetMiniLeaderboardDTO;
import com.sap.sailing.gwt.ui.client.CompetitorSelectionModel;
import com.sap.sailing.gwt.ui.client.shared.racemap.RaceMap;

public class Slide7Place extends Place {
    public static class Tokenizer implements PlaceTokenizer<Slide7Place> {
        @Override
        public String getToken(Slide7Place place) {
            return "";
        }

        @Override
        public Slide7Place getPlace(String token) {
            return new Slide7Place();
        }
    }

    private GetMiniLeaderboardDTO leaderBoardDTO;
    private RegattaAndRaceIdentifier lifeRace;
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

    public void setLifeRace(RegattaAndRaceIdentifier lifeRace) {
        this.lifeRace = lifeRace;
    }

    public RegattaAndRaceIdentifier getLifeRace() {
        return lifeRace;
    }
}
