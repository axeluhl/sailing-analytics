package com.sap.sailing.gwt.home.client.place.event2.tabs.overview.multiregatta;

import com.google.gwt.place.shared.PlaceTokenizer;
import com.sap.sailing.gwt.home.client.place.event2.multiregatta.AbstractMultiregattaEventPlace;
import com.sap.sailing.gwt.home.client.place.event2.tabs.EventContext;

public class MultiRegattaOverviewPlace extends AbstractMultiregattaEventPlace {
    public MultiRegattaOverviewPlace(String id) {
        super(id);
    }
    
    public MultiRegattaOverviewPlace(EventContext context) {
        super(context);
    }

    public static class Tokenizer implements PlaceTokenizer<MultiRegattaOverviewPlace> {
        @Override
        public String getToken(MultiRegattaOverviewPlace place) {
            return place.getEventUuidAsString();
        }

        @Override
        public MultiRegattaOverviewPlace getPlace(String token) {
            return new MultiRegattaOverviewPlace(token);
        }
    }
}
