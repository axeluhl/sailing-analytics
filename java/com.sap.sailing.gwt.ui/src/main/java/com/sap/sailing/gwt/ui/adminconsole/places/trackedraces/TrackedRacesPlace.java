package com.sap.sailing.gwt.ui.adminconsole.places.trackedraces;

import com.google.gwt.place.shared.PlaceTokenizer;

public class TrackedRacesPlace extends AbstractTrackedRacesPlace {
    
    public TrackedRacesPlace() {   
    }
    
    public static class Tokenizer implements PlaceTokenizer<TrackedRacesPlace> {
        @Override
        public String getToken(final TrackedRacesPlace place) {
            return "";
        }

        @Override
        public TrackedRacesPlace getPlace(final String token) {
            return new TrackedRacesPlace();
        }
    }
    
}
