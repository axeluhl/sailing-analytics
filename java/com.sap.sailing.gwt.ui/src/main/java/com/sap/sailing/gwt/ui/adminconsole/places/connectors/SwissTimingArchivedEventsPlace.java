package com.sap.sailing.gwt.ui.adminconsole.places.connectors;

import com.google.gwt.place.shared.PlaceTokenizer;

public class SwissTimingArchivedEventsPlace extends AbstractConnectorsPlace {
    
    public SwissTimingArchivedEventsPlace() { 
    }

    public static class Tokenizer implements PlaceTokenizer<SwissTimingArchivedEventsPlace> {
        @Override
        public String getToken(final SwissTimingArchivedEventsPlace place) {
            return "";
        }

        @Override
        public SwissTimingArchivedEventsPlace getPlace(final String token) {
            return new SwissTimingArchivedEventsPlace();
        }
    }
    
}
