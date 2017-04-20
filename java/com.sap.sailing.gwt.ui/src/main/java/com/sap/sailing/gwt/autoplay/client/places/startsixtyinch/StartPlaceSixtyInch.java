package com.sap.sailing.gwt.autoplay.client.place.sixtyinch.places.start;

import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceTokenizer;

public class StartPlaceSixtyInch extends Place {
    public static class Tokenizer implements PlaceTokenizer<StartPlaceSixtyInch> {
        @Override
        public String getToken(StartPlaceSixtyInch place) {
            return null;
        }

        @Override
        public StartPlaceSixtyInch getPlace(String token) {
            return new StartPlaceSixtyInch();
        }
    }
}
