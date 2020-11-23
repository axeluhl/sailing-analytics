package com.sap.sailing.gwt.ui.adminconsole.places.events;

import java.util.function.Supplier;

import com.sap.sailing.gwt.ui.adminconsole.places.AbstractFilterablePlace;


public class EventsPlace extends AbstractFilterablePlace {

    public static class Tokenizer extends TablePlaceTokenizer<EventsPlace> {      

        @Override
        protected Supplier<EventsPlace> getPlaceFactory() {
            return EventsPlace::new;
        }
    }
    
}
