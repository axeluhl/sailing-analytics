package com.sap.sailing.gwt.autoplay.client.places.autoplaystart;

import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceTokenizer;

public class AutoPlayStartPlace extends Place {
    public static class Tokenizer implements PlaceTokenizer<AutoPlayStartPlace> {
        @Override
        public String getToken(AutoPlayStartPlace place) {
            return null;
        }

        @Override
        public AutoPlayStartPlace getPlace(String token) {
            return new AutoPlayStartPlace();
        }
    }
}
