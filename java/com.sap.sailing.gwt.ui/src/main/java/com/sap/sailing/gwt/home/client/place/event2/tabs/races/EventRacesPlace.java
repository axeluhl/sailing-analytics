package com.sap.sailing.gwt.home.client.place.event2.tabs.races;

import com.google.gwt.place.shared.PlaceTokenizer;
import com.sap.sailing.gwt.home.client.place.event2.AbstractEventPlace;
import com.sap.sailing.gwt.home.client.place.event2.tabs.EventContext;

public class EventRacesPlace extends AbstractEventPlace {
    public EventRacesPlace(String id) {
        super(id);
    }
    
    public EventRacesPlace(EventContext context) {
        super(context);
    }

    public static class Tokenizer implements PlaceTokenizer<EventRacesPlace> {
        @Override
        public String getToken(EventRacesPlace place) {
            return place.getEventUuidAsString();
        }

        @Override
        public EventRacesPlace getPlace(String token) {
            return new EventRacesPlace(token);
        }
    }
}
