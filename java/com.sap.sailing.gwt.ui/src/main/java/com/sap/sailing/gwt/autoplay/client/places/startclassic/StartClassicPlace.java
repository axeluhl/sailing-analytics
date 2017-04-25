package com.sap.sailing.gwt.autoplay.client.places.startclassic;

import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceTokenizer;

public class StartClassicPlace extends Place {
    public static class Tokenizer implements PlaceTokenizer<StartClassicPlace> {
        @Override
        public String getToken(StartClassicPlace place) {
            return null;
        }

        @Override
        public StartClassicPlace getPlace(String token) {
            return new StartClassicPlace();
        }
    }
}
