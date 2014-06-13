package com.sap.sailing.gwt.home.client.place.events;

import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceTokenizer;

public class EventsPlace extends Place {
    public static class Tokenizer implements PlaceTokenizer<EventsPlace> {
        @Override
        public String getToken(EventsPlace place) {
            return null;
        }

        @Override
        public EventsPlace getPlace(String token) {
            return new EventsPlace();
        }
    }
}
