package com.sap.sailing.gwt.ui.adminconsole.places.connectors;

import com.google.gwt.place.shared.PlaceTokenizer;

public class SwissTimingEventsPlace extends AbstractConnectorsPlace {
    
    public SwissTimingEventsPlace() { 
    }

    public static class Tokenizer implements PlaceTokenizer<SwissTimingEventsPlace> {
        @Override
        public String getToken(final SwissTimingEventsPlace place) {
            return "";
        }

        @Override
        public SwissTimingEventsPlace getPlace(final String token) {
            return new SwissTimingEventsPlace();
        }
    }
    
}
