package com.sap.sailing.gwt.ui.adminconsole.places.events;

import java.util.function.Function;

import com.sap.sailing.gwt.ui.adminconsole.places.AbstractFilterablePlace;


public class EventsPlace extends AbstractFilterablePlace {
    public EventsPlace(String token) {
        super(token);
    }

    public static class Tokenizer extends TablePlaceTokenizer<EventsPlace> {      
        @Override
        protected Function<String, EventsPlace> getPlaceFactory() {
            return EventsPlace::new;
        }
    }
}
