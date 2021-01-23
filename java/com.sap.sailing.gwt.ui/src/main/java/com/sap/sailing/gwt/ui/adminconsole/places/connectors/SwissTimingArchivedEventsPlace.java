package com.sap.sailing.gwt.ui.adminconsole.places.connectors;

import java.util.function.Function;

import com.sap.sailing.gwt.ui.adminconsole.places.AbstractFilterablePlace;

public class SwissTimingArchivedEventsPlace extends AbstractFilterablePlace {
    public SwissTimingArchivedEventsPlace(String token) {
        super(token);
    }
    
    public static class Tokenizer extends TablePlaceTokenizer<SwissTimingArchivedEventsPlace> {      
        @Override
        protected Function<String, SwissTimingArchivedEventsPlace> getPlaceFactory() {
            return SwissTimingArchivedEventsPlace::new;
        }
    }
}
