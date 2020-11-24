package com.sap.sailing.gwt.ui.adminconsole.places.connectors;

import java.util.function.Function;

import com.sap.sailing.gwt.ui.adminconsole.places.AbstractFilterablePlace;
import com.sap.sailing.gwt.ui.adminconsole.places.AdminConsoleViewImpl;

public class TracTracEventsPlace extends AbstractFilterablePlace {
    public TracTracEventsPlace(String token) {
        super(token);
    }

    @Override
    public String getVerticalTabName() {
        return AdminConsoleViewImpl.CONNECTORS;
    }
    
    public static class Tokenizer extends TablePlaceTokenizer<TracTracEventsPlace> {      

        @Override
        protected Function<String, TracTracEventsPlace> getPlaceFactory() {
            return TracTracEventsPlace::new;
        }
    }
}
