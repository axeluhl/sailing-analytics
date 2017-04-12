package com.sap.sailing.gwt.autoplay.client.place.sixtyinch.slides.slide0;

import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceTokenizer;
import com.sap.sailing.gwt.home.communication.event.minileaderboard.GetMiniLeaderboardDTO;

public class PreRaceLeaderBoardWithFlagsPlace extends Place {

    private GetMiniLeaderboardDTO leaderBoardDTO;

    public GetMiniLeaderboardDTO getLeaderBoardDTO() {
        return leaderBoardDTO;
    }

    public void setLeaderBoardDTO(GetMiniLeaderboardDTO leaderBoardDTO) {
        this.leaderBoardDTO = leaderBoardDTO;
    }

    public static class Tokenizer implements PlaceTokenizer<PreRaceLeaderBoardWithFlagsPlace> {
        @Override
        public String getToken(PreRaceLeaderBoardWithFlagsPlace place) {
            return "";
        }

        @Override
        public PreRaceLeaderBoardWithFlagsPlace getPlace(String token) {
            return new PreRaceLeaderBoardWithFlagsPlace();
        }
    }
}
