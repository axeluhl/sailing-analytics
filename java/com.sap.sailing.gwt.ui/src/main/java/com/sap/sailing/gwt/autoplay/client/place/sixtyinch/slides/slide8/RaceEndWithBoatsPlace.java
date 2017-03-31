package com.sap.sailing.gwt.autoplay.client.place.sixtyinch.slides.slide8;

import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceTokenizer;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.gwt.home.communication.event.minileaderboard.GetMiniLeaderboardDTO;

public class RaceEndWithBoatsPlace extends Place {

    private GetMiniLeaderboardDTO leaderBoardDTO;
    private RegattaAndRaceIdentifier lifeRace;

    public GetMiniLeaderboardDTO getLeaderBoardDTO() {
        return leaderBoardDTO;
    }

    public void setLeaderBoardDTO(GetMiniLeaderboardDTO leaderBoardDTO) {
        this.leaderBoardDTO = leaderBoardDTO;
    }

    public RegattaAndRaceIdentifier getLastRace() {
        return lifeRace;
    }

    public static class Tokenizer implements PlaceTokenizer<RaceEndWithBoatsPlace> {
        @Override
        public String getToken(RaceEndWithBoatsPlace place) {
            return "";
        }

        @Override
        public RaceEndWithBoatsPlace getPlace(String token) {
            return new RaceEndWithBoatsPlace();
        }
    }

    public void setLifeRace(RegattaAndRaceIdentifier lifeRace) {
        this.lifeRace = lifeRace;
    }
}
