package com.sap.sailing.gwt.home.client.place.event2.multiregatta.tabs.media;

import com.google.gwt.place.shared.PlaceTokenizer;
import com.sap.sailing.gwt.home.client.place.event2.EventContext;
import com.sap.sailing.gwt.home.client.place.event2.multiregatta.AbstractMultiregattaEventPlace;

public class MultiRegattaMediaPlace extends AbstractMultiregattaEventPlace {
    public MultiRegattaMediaPlace(String id) {
        super(id);
    }
    
    public MultiRegattaMediaPlace(EventContext context) {
        super(context);
    }

    public static class Tokenizer implements PlaceTokenizer<MultiRegattaMediaPlace> {
        @Override
        public String getToken(MultiRegattaMediaPlace place) {
            return place.getEventUuidAsString();
        }

        @Override
        public MultiRegattaMediaPlace getPlace(String token) {
            return new MultiRegattaMediaPlace(token);
        }
    }
}
