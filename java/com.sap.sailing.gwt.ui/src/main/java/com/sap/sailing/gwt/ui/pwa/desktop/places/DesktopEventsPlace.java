package com.sap.sailing.gwt.ui.pwa.desktop.places;

import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceTokenizer;

public class DesktopEventsPlace extends Place {
    
    public DesktopEventsPlace() {

    }
    
    public static class Tokenizer implements PlaceTokenizer<DesktopEventsPlace> {
        @Override
        public String getToken(final DesktopEventsPlace place) {
            return "";
        }

        @Override
        public DesktopEventsPlace getPlace(final String token) { 
            return new DesktopEventsPlace();
        }
    }
    
}
