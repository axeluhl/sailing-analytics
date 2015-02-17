package com.sap.sailing.gwt.home.client.place.event2.tabs.races;

import com.google.gwt.place.shared.PlaceTokenizer;
import com.sap.sailing.gwt.home.client.place.event2.regatta.AbstractEventRegattaPlace;
import com.sap.sailing.gwt.home.client.place.event2.tabs.EventContext;

public class EventRacesPlace extends AbstractEventRegattaPlace {
    public EventRacesPlace(String id, String regattaId) {
        super(id, regattaId);
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
            // TODO
            return new EventRacesPlace(token, "");
        }
    }
}
