package com.sap.sailing.gwt.ui.adminconsole.places.connectors;

import java.util.function.Function;

import com.sap.sailing.gwt.ui.adminconsole.places.AbstractFilterablePlace;
import com.sap.sailing.gwt.ui.adminconsole.places.AdminConsoleViewImpl;

public class SwissTimingArchivedEventsPlace extends AbstractFilterablePlace {
    public SwissTimingArchivedEventsPlace(String token) {
        super(token);
    }

    @Override
    public String getVerticalTabName() {
        return AdminConsoleViewImpl.CONNECTORS;
    }
    
    public static class Tokenizer extends TablePlaceTokenizer<SwissTimingArchivedEventsPlace> {      
        @Override
        protected Function<String, SwissTimingArchivedEventsPlace> getPlaceFactory() {
            return SwissTimingArchivedEventsPlace::new;
        }
    }
}
