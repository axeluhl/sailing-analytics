package com.sap.sailing.gwt.home.client.place.event2.multiregatta.tabs;

import com.google.gwt.place.shared.PlaceTokenizer;
import com.sap.sailing.gwt.home.client.place.event2.EventContext;
import com.sap.sailing.gwt.home.client.place.event2.multiregatta.AbstractMultiregattaEventPlace;

public class MultiregattaMediaPlace extends AbstractMultiregattaEventPlace {
    public MultiregattaMediaPlace(String id) {
        super(id);
    }
    
    public MultiregattaMediaPlace(EventContext context) {
        super(context);
    }

    public static class Tokenizer implements PlaceTokenizer<MultiregattaMediaPlace> {
        @Override
        public String getToken(MultiregattaMediaPlace place) {
            return place.getEventUuidAsString();
        }

        @Override
        public MultiregattaMediaPlace getPlace(String token) {
            return new MultiregattaMediaPlace(token);
        }
    }
}
