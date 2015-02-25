package com.sap.sailing.gwt.home.client.place.event2.multiregatta.tabs;

import com.google.gwt.place.shared.PlaceTokenizer;
import com.sap.sailing.gwt.home.client.place.event2.EventContext;
import com.sap.sailing.gwt.home.client.place.event2.multiregatta.AbstractMultiregattaEventPlace;

public class MultiregattaRegattasPlace extends AbstractMultiregattaEventPlace {
    public MultiregattaRegattasPlace(String id) {
        super(id);
    }
    
    public MultiregattaRegattasPlace(EventContext context) {
        super(context);
    }

    public static class Tokenizer implements PlaceTokenizer<MultiregattaRegattasPlace> {
        @Override
        public String getToken(MultiregattaRegattasPlace place) {
            return place.getEventUuidAsString();
        }

        @Override
        public MultiregattaRegattasPlace getPlace(String token) {
            return new MultiregattaRegattasPlace(token);
        }
    }
}
