package com.sap.sailing.gwt.ui.adminconsole.places.advanced;

import com.google.gwt.place.shared.PlaceTokenizer;

public class LocalServerPlace extends AbstractAdvancedPlace {
    public LocalServerPlace(String token) {
        super(token);
    }

    public static class Tokenizer implements PlaceTokenizer<LocalServerPlace> {
        @Override
        public String getToken(final LocalServerPlace place) {
            return place.getParametersAsToken();
        }

        @Override
        public LocalServerPlace getPlace(final String token) {
            return new LocalServerPlace(token);
        }
    }
    
}
