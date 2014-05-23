package com.sap.sailing.gwt.home.client.app.start;

import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceTokenizer;

public class StartPlace extends Place {
    public static class Tokenizer implements PlaceTokenizer<StartPlace> {
        @Override
        public String getToken(StartPlace place) {
            return null;
        }

        @Override
        public StartPlace getPlace(String token) {
            return new StartPlace();
        }
    }
}
