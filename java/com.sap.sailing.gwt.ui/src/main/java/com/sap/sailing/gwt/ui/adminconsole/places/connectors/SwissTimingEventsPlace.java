package com.sap.sailing.gwt.ui.adminconsole.places.connectors;

import java.util.function.Function;

import com.sap.sailing.gwt.ui.adminconsole.places.AbstractFilterablePlace;
import com.sap.sailing.gwt.ui.adminconsole.places.AdminConsoleViewImpl;

public class SwissTimingEventsPlace extends AbstractFilterablePlace {
    public SwissTimingEventsPlace(String token) {
        super(token);
    }

    @Override
    public String getVerticalTabName() {
        return AdminConsoleViewImpl.CONNECTORS;
    }
    
    public static class Tokenizer extends TablePlaceTokenizer<SwissTimingEventsPlace> {      
        @Override
        protected Function<String, SwissTimingEventsPlace> getPlaceFactory() {
            return SwissTimingEventsPlace::new;
        }
    }
}
