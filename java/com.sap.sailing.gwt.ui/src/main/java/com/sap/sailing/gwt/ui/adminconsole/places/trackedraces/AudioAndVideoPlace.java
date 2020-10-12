package com.sap.sailing.gwt.ui.adminconsole.places.trackedraces;

import com.google.gwt.place.shared.PlaceTokenizer;

public class AudioAndVideoPlace extends AbstractTrackedRacesPlace {
    
    public AudioAndVideoPlace() {
    }
    
    public static class Tokenizer implements PlaceTokenizer<AudioAndVideoPlace> {
        @Override
        public String getToken(final AudioAndVideoPlace place) {
            return "";
        }

        @Override
        public AudioAndVideoPlace getPlace(final String token) {
            return new AudioAndVideoPlace();
        }
    }
    
}
