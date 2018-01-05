package com.sap.sailing.gwt.autoplay.client.places.screens.preliveraceloop.leaderboard;

import com.google.gwt.place.shared.PlaceTokenizer;

public class PreRaceLeaderBoardWithFlagPlace extends AbstractPreRaceLeaderBoardWithImagePlace {
    public static class Tokenizer implements PlaceTokenizer<PreRaceLeaderBoardWithFlagPlace> {
        @Override
        public String getToken(PreRaceLeaderBoardWithFlagPlace place) {
            return "";
        }

        @Override
        public PreRaceLeaderBoardWithFlagPlace getPlace(String token) {
            return new PreRaceLeaderBoardWithFlagPlace();
        }
    }
}
