package com.sap.sailing.gwt.ui.adminconsole.places.advanced;

import com.google.gwt.place.shared.PlaceTokenizer;

public class LocalServerPlace extends AbstractAdvancedPlace {
    
    public LocalServerPlace() { 
    }

    public static class Tokenizer implements PlaceTokenizer<LocalServerPlace> {
        @Override
        public String getToken(final LocalServerPlace place) {
            return "";
        }

        @Override
        public LocalServerPlace getPlace(final String token) {
            return new LocalServerPlace();
        }
    }
    
}
