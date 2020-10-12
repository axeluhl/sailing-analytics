package com.sap.sailing.gwt.ui.adminconsole.places.trackedraces;

import com.google.gwt.place.shared.PlaceTokenizer;

public class WindPlace extends AbstractTrackedRacesPlace {
    
    public WindPlace() {
    }
    
    public static class Tokenizer implements PlaceTokenizer<WindPlace> {
        @Override
        public String getToken(final WindPlace place) {
            return "";
        }

        @Override
        public WindPlace getPlace(final String token) {
            return new WindPlace();
        }
    }
    
}
