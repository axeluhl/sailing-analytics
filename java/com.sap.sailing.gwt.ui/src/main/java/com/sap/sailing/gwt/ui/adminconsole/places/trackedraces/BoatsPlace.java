package com.sap.sailing.gwt.ui.adminconsole.places.trackedraces;

import com.google.gwt.place.shared.PlaceTokenizer;

public class BoatsPlace extends AbstractTrackedRacesPlace {
    
    public BoatsPlace() {
    }
    
    public static class Tokenizer implements PlaceTokenizer<BoatsPlace> {
        @Override
        public String getToken(final BoatsPlace place) {
            return "";
        }

        @Override
        public BoatsPlace getPlace(final String token) {
            return new BoatsPlace();
        }
    }
    
}
