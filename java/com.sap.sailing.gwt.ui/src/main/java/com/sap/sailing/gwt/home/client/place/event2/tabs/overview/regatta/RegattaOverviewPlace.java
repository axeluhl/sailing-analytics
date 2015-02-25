package com.sap.sailing.gwt.home.client.place.event2.tabs.overview.regatta;

import com.google.gwt.place.shared.PlaceTokenizer;
import com.sap.sailing.gwt.home.client.place.event2.regatta.AbstractEventRegattaPlace;
import com.sap.sailing.gwt.home.client.place.event2.tabs.EventContext;

public class RegattaOverviewPlace extends AbstractEventRegattaPlace {
    public RegattaOverviewPlace(String id, String regattaId) {
        super(id, regattaId);
    }
    
    public RegattaOverviewPlace(EventContext context) {
        super(context);
    }

    public static class Tokenizer implements PlaceTokenizer<RegattaOverviewPlace> {
        @Override
        public String getToken(RegattaOverviewPlace place) {
            return place.getEventUuidAsString();
        }

        @Override
        public RegattaOverviewPlace getPlace(String token) {
            // TODO
            return new RegattaOverviewPlace(token, "");
        }
    }
}
