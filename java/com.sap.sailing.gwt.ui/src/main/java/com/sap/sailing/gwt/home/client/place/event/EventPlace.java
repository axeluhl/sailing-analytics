package com.sap.sailing.gwt.home.client.place.event;

import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceTokenizer;

public class EventPlace extends Place {
    private final String eventUuidAsString;
    
    public EventPlace(String eventUuidAsString) {
        super();
        this.eventUuidAsString = eventUuidAsString;
    }

    public String getEventUuidAsString() {
        return eventUuidAsString;
    }

    public static class Tokenizer implements PlaceTokenizer<EventPlace> {
        @Override
        public String getToken(EventPlace place) {
            return place.getEventUuidAsString();
        }

        @Override
        public EventPlace getPlace(String eventUuidAsString) {
            return new EventPlace(eventUuidAsString);
        }
    }
}
