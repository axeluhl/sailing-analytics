package com.sap.sailing.gwt.autoplay.client.places.screens.preliveraceloop.leaderboard;

import com.google.gwt.place.shared.PlaceTokenizer;

public class PreRaceLeaderboardWithCompetitorPlace extends AbstractPreRaceLeaderboardWithImagePlace {
    public static class Tokenizer implements PlaceTokenizer<PreRaceLeaderboardWithCompetitorPlace> {
        @Override
        public String getToken(PreRaceLeaderboardWithCompetitorPlace place) {
            return "";
        }

        @Override
        public PreRaceLeaderboardWithCompetitorPlace getPlace(String token) {
            return new PreRaceLeaderboardWithCompetitorPlace();
        }
    }
}
