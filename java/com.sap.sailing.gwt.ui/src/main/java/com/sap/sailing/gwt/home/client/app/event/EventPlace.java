package com.sap.sailing.gwt.home.client.app.event;

import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceTokenizer;

public class EventPlace extends Place {
    public static class Tokenizer implements PlaceTokenizer<EventPlace> {
        @Override
        public String getToken(EventPlace place) {
            return null;
        }

        @Override
        public EventPlace getPlace(String token) {
            return new EventPlace();
        }
    }
}
