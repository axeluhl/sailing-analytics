package com.sap.sailing.gwt.home.client.place.event2.tabs.overview;

import com.google.gwt.place.shared.PlaceTokenizer;
import com.sap.sailing.gwt.home.client.place.event2.regatta.AbstractEventRegattaPlace;
import com.sap.sailing.gwt.home.client.place.event2.tabs.EventContext;

public class EventRegattaOverviewPlace extends AbstractEventRegattaPlace {
    public EventRegattaOverviewPlace(String id, String regattaId) {
        super(id, regattaId);
    }
    
    public EventRegattaOverviewPlace(EventContext context) {
        super(context);
    }

    public static class Tokenizer implements PlaceTokenizer<EventRegattaOverviewPlace> {
        @Override
        public String getToken(EventRegattaOverviewPlace place) {
            return place.getEventUuidAsString();
        }

        @Override
        public EventRegattaOverviewPlace getPlace(String token) {
            // TODO
            return new EventRegattaOverviewPlace(token, "");
        }
    }
}
