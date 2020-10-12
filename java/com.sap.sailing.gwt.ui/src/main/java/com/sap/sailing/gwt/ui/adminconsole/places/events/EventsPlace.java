package com.sap.sailing.gwt.ui.adminconsole.places.events;

import com.google.gwt.place.shared.PlaceTokenizer;
import com.sap.sailing.gwt.ui.adminconsole.places.AbstractAdminConsolePlace;


public class EventsPlace extends AbstractAdminConsolePlace {
    
    public EventsPlace() {
    }
    
    public static class Tokenizer implements PlaceTokenizer<EventsPlace> {
        @Override
        public String getToken(final EventsPlace place) {
            return "";
        }

        @Override
        public EventsPlace getPlace(final String token) {
            return new EventsPlace();
        }
    }

    @Override
    public String getVerticalTabName() {
        return "";
    }
    
}
