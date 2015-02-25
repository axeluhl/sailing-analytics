package com.sap.sailing.gwt.home.client.place.event2.tabs.regattas;

import com.google.gwt.place.shared.PlaceTokenizer;
import com.sap.sailing.gwt.home.client.place.event2.regatta.AbstractEventRegattaPlace;
import com.sap.sailing.gwt.home.client.place.event2.tabs.EventContext;

public class RegattasPlace extends AbstractEventRegattaPlace {
    public RegattasPlace(String id, String regattaId) {
        super(id, regattaId);
    }
    
    public RegattasPlace(EventContext context) {
        super(context);
    }

    public static class Tokenizer implements PlaceTokenizer<RegattasPlace> {
        @Override
        public String getToken(RegattasPlace place) {
            return place.getEventUuidAsString();
        }

        @Override
        public RegattasPlace getPlace(String token) {
            // TODO
            return new RegattasPlace(token, "");
        }
    }
}
