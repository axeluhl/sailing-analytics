package com.sap.sailing.gwt.home.client.app.event;

import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceTokenizer;

public class EventPlace extends Place {
    private final String eventId;
    
    protected EventPlace(String eventId) {
        super();
        this.eventId = eventId;
    }

    public String getEventId() {
        return eventId;
    }

    public static class Tokenizer implements PlaceTokenizer<EventPlace> {
        @Override
        public String getToken(EventPlace place) {
            return place.getEventId();
        }

        @Override
        public EventPlace getPlace(String eventId) {
            return new EventPlace(eventId);
        }
    }
}
