package com.sap.sailing.gwt.home.client.place.event2.multiregatta.tabs.overview;

import com.google.gwt.place.shared.PlaceTokenizer;
import com.sap.sailing.gwt.home.client.place.event2.multiregatta.AbstractMultiregattaEventPlace;
import com.sap.sailing.gwt.home.client.place.event2.tabs.EventContext;

public class EventOverviewPlace extends AbstractMultiregattaEventPlace {
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
