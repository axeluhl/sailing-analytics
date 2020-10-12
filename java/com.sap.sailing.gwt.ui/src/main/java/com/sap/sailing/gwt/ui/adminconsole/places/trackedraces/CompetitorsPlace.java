package com.sap.sailing.gwt.ui.adminconsole.places.trackedraces;

import com.google.gwt.place.shared.PlaceTokenizer;

public class CompetitorsPlace extends AbstractTrackedRacesPlace {
    
    public CompetitorsPlace() {
    }
    
    public static class Tokenizer implements PlaceTokenizer<CompetitorsPlace> {
        @Override
        public String getToken(final CompetitorsPlace place) {
            return "";
        }

        @Override
        public CompetitorsPlace getPlace(final String token) {
            return new CompetitorsPlace();
        }
    }
    
}
