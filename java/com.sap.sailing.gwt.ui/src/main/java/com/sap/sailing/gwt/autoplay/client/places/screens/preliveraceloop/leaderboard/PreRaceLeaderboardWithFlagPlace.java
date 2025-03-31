package com.sap.sailing.gwt.autoplay.client.places.screens.preliveraceloop.leaderboard;

import com.google.gwt.place.shared.PlaceTokenizer;

public class PreRaceLeaderboardWithFlagPlace extends AbstractPreRaceLeaderboardWithImagePlace {
    public static class Tokenizer implements PlaceTokenizer<PreRaceLeaderboardWithFlagPlace> {
        @Override
        public String getToken(PreRaceLeaderboardWithFlagPlace place) {
            return "";
        }

        @Override
        public PreRaceLeaderboardWithFlagPlace getPlace(String token) {
            return new PreRaceLeaderboardWithFlagPlace();
        }
    }
}
