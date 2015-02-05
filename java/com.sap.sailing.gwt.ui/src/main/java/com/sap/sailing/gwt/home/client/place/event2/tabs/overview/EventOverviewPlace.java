package com.sap.sailing.gwt.home.client.place.event2.tabs.overview;

import com.google.gwt.place.shared.PlaceTokenizer;
import com.sap.sailing.gwt.home.client.place.event2.EventPlace;
import com.sap.sailing.gwt.home.client.place.event2.tabs.EventContext;

public class EventOverviewPlace extends EventPlace {
    public EventOverviewPlace(String id) {
        super(id);
    }
    
    public EventOverviewPlace(EventContext context) {
        super(context);
    }

    public static class Tokenizer implements PlaceTokenizer<EventOverviewPlace> {
        @Override
        public String getToken(EventOverviewPlace place) {
            return place.getEventUuidAsString();
        }

        @Override
        public EventOverviewPlace getPlace(String token) {
            return new EventOverviewPlace(token);
        }
    }
}
