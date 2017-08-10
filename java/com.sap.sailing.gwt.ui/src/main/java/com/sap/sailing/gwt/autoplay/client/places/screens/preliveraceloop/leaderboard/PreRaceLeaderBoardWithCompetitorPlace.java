package com.sap.sailing.gwt.autoplay.client.places.screens.preliveraceloop.leaderboard;

import com.google.gwt.place.shared.PlaceTokenizer;

public class PreRaceLeaderBoardWithCompetitorPlace extends AbstractPreRaceLeaderBoardWithImagePlace {
    public static class Tokenizer implements PlaceTokenizer<PreRaceLeaderBoardWithCompetitorPlace> {
        @Override
        public String getToken(PreRaceLeaderBoardWithCompetitorPlace place) {
            return "";
        }

        @Override
        public PreRaceLeaderBoardWithCompetitorPlace getPlace(String token) {
            return new PreRaceLeaderBoardWithCompetitorPlace();
        }
    }
}
