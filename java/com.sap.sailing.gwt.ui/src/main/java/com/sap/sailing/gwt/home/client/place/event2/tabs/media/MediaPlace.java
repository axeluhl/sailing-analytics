package com.sap.sailing.gwt.home.client.place.event2.tabs.media;

import com.google.gwt.place.shared.PlaceTokenizer;
import com.sap.sailing.gwt.home.client.place.event2.regatta.AbstractEventRegattaPlace;
import com.sap.sailing.gwt.home.client.place.event2.tabs.EventContext;

public class MediaPlace extends AbstractEventRegattaPlace {
    public MediaPlace(String id, String regattaId) {
        super(id, regattaId);
    }
    
    public MediaPlace(EventContext context) {
        super(context);
    }

    public static class Tokenizer implements PlaceTokenizer<MediaPlace> {
        @Override
        public String getToken(MediaPlace place) {
            return place.getEventUuidAsString();
        }

        @Override
        public MediaPlace getPlace(String token) {
            // TODO
            return new MediaPlace(token, "");
        }
    }
}
