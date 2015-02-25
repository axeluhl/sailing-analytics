package com.sap.sailing.gwt.home.client.place.event2.multiregatta.tabs.regattas;

import com.google.gwt.place.shared.PlaceTokenizer;
import com.sap.sailing.gwt.home.client.place.event2.EventContext;
import com.sap.sailing.gwt.home.client.place.event2.multiregatta.AbstractMultiregattaEventPlace;

public class EventRegattasPlace extends AbstractMultiregattaEventPlace {
    public EventRegattasPlace(String id) {
        super(id);
    }
    
    public EventRegattasPlace(EventContext context) {
        super(context);
    }

    public static class Tokenizer implements PlaceTokenizer<EventRegattasPlace> {
        @Override
        public String getToken(EventRegattasPlace place) {
            return place.getEventUuidAsString();
        }

        @Override
        public EventRegattasPlace getPlace(String token) {
            return new EventRegattasPlace(token);
        }
    }
}
