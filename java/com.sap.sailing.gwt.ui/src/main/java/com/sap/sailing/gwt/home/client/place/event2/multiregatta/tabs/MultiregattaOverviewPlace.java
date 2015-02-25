package com.sap.sailing.gwt.home.client.place.event2.multiregatta.tabs;

import com.google.gwt.place.shared.PlaceTokenizer;
import com.sap.sailing.gwt.home.client.place.event2.EventContext;
import com.sap.sailing.gwt.home.client.place.event2.multiregatta.AbstractMultiregattaEventPlace;

public class MultiregattaOverviewPlace extends AbstractMultiregattaEventPlace {
    public MultiregattaOverviewPlace(String id) {
        super(id);
    }
    
    public MultiregattaOverviewPlace(EventContext context) {
        super(context);
    }

    public static class Tokenizer implements PlaceTokenizer<MultiregattaOverviewPlace> {
        @Override
        public String getToken(MultiregattaOverviewPlace place) {
            return place.getEventUuidAsString();
        }

        @Override
        public MultiregattaOverviewPlace getPlace(String token) {
            return new MultiregattaOverviewPlace(token);
        }
    }
}
