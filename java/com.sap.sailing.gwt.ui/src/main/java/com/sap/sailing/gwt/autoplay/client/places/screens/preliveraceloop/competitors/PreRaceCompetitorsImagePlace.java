package com.sap.sailing.gwt.autoplay.client.places.screens.preliveraceloop.competitors;

import com.google.gwt.place.shared.PlaceTokenizer;

public class PreRaceCompetitorsImagePlace extends AbstractPreRaceCompetitorsPlace {
    public static class Tokenizer implements PlaceTokenizer<PreRaceCompetitorsImagePlace> {
        @Override
        public String getToken(PreRaceCompetitorsImagePlace place) {
            return "";
        }

        @Override
        public PreRaceCompetitorsImagePlace getPlace(String token) {
            return new PreRaceCompetitorsImagePlace();
        }
    }
}
