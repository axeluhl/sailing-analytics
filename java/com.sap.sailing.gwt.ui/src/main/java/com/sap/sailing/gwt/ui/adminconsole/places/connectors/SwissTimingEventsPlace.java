package com.sap.sailing.gwt.ui.adminconsole.places.connectors;

import java.util.function.Function;

import com.sap.sse.gwt.adminconsole.AbstractFilterablePlace;

public class SwissTimingEventsPlace extends AbstractFilterablePlace {
    public SwissTimingEventsPlace(String token) {
        super(token);
    }
    
    public static class Tokenizer extends TablePlaceTokenizer<SwissTimingEventsPlace> {      
        @Override
        protected Function<String, SwissTimingEventsPlace> getPlaceFactory() {
            return SwissTimingEventsPlace::new;
        }
    }
}
