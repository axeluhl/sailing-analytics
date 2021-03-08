package com.sap.sailing.gwt.ui.adminconsole.places.trackedraces;

import java.util.function.Function;

import com.sap.sse.gwt.adminconsole.AbstractFilterablePlace;

public class TrackedRacesPlace extends AbstractFilterablePlace {
    public TrackedRacesPlace(String token) {
        super(token);
    }
    
    public static class Tokenizer extends TablePlaceTokenizer<TrackedRacesPlace> {      
        @Override
        protected Function<String, TrackedRacesPlace> getPlaceFactory() {
            return TrackedRacesPlace::new;
        }
    }
}
