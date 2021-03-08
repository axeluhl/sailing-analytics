package com.sap.sailing.gwt.ui.adminconsole.places.connectors;

import java.util.function.Function;

import com.sap.sse.gwt.adminconsole.AbstractFilterablePlace;

public class TracTracEventsPlace extends AbstractFilterablePlace {
    public TracTracEventsPlace(String token) {
        super(token);
    }
    
    public static class Tokenizer extends TablePlaceTokenizer<TracTracEventsPlace> {      

        @Override
        protected Function<String, TracTracEventsPlace> getPlaceFactory() {
            return TracTracEventsPlace::new;
        }
    }
}
