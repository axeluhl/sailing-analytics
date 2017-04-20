package com.sap.sailing.gwt.autoplay.client.place.sixtyinch.slides.slide4;

import com.google.gwt.place.shared.PlaceTokenizer;

public class PreRaceCompetitorsFlagsPlace extends AbstractPreRaceCompetitorsPlace {
    public static class Tokenizer implements PlaceTokenizer<PreRaceCompetitorsFlagsPlace> {
        @Override
        public String getToken(PreRaceCompetitorsFlagsPlace place) {
            return "";
        }

        @Override
        public PreRaceCompetitorsFlagsPlace getPlace(String token) {
            return new PreRaceCompetitorsFlagsPlace();
        }
    }
}
