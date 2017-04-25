package com.sap.sailing.gwt.autoplay.client.places.startup.sixtyinch.config;

import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceTokenizer;

public class SixtyInchConfigPlace extends Place {
    public static class Tokenizer implements PlaceTokenizer<SixtyInchConfigPlace> {
        @Override
        public String getToken(SixtyInchConfigPlace place) {
            return null;
        }

        @Override
        public SixtyInchConfigPlace getPlace(String token) {
            return new SixtyInchConfigPlace();
        }
    }
}
