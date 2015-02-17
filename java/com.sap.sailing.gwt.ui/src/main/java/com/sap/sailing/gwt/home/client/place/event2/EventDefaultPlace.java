package com.sap.sailing.gwt.home.client.place.event2;

import com.google.gwt.place.shared.PlaceTokenizer;
import com.sap.sailing.gwt.home.client.place.event2.tabs.EventContext;

public class EventDefaultPlace extends AbstractEventPlace {

    public EventDefaultPlace(EventContext ctx) {
        super(ctx);
    }

    public EventDefaultPlace(String eventUuidAsString) {
        super(eventUuidAsString);
    }

    public static class Tokenizer implements PlaceTokenizer<EventDefaultPlace> {
        @Override
        public String getToken(EventDefaultPlace place) {
            return place.getEventUuidAsString();
        }

        @Override
        public EventDefaultPlace getPlace(String token) {
            return new EventDefaultPlace(token);
        }
    }
}
