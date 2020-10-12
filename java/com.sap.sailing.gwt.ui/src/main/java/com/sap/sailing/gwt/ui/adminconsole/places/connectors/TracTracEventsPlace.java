package com.sap.sailing.gwt.ui.adminconsole.places.connectors;

import com.google.gwt.place.shared.PlaceTokenizer;

public class TracTracEventsPlace extends AbstractConnectorsPlace {
    
    public TracTracEventsPlace() { 
    }

    public static class Tokenizer implements PlaceTokenizer<TracTracEventsPlace> {
        @Override
        public String getToken(final TracTracEventsPlace place) {
            return "";
        }

        @Override
        public TracTracEventsPlace getPlace(final String token) {
            return new TracTracEventsPlace();
        }
    }
    
}
