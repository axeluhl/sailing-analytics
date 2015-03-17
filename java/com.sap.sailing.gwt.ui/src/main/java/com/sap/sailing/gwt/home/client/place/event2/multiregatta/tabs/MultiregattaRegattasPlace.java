package com.sap.sailing.gwt.home.client.place.event2.multiregatta.tabs;

import com.google.gwt.place.shared.Prefix;
import com.sap.sailing.gwt.home.client.place.event2.AbstractEventPlace;
import com.sap.sailing.gwt.home.client.place.event2.EventContext;
import com.sap.sailing.gwt.home.client.place.event2.EventPrefixes;
import com.sap.sailing.gwt.home.client.place.event2.multiregatta.AbstractMultiregattaEventPlace;

public class MultiregattaRegattasPlace extends AbstractMultiregattaEventPlace {
    public MultiregattaRegattasPlace(String id) {
        super(id);
    }
    
    public MultiregattaRegattasPlace(EventContext context) {
        super(context);
    }

    @Prefix(EventPrefixes.MultiregattaRegattas)
    public static class Tokenizer extends AbstractEventPlace.Tokenizer<MultiregattaRegattasPlace> {
        @Override
        protected MultiregattaRegattasPlace getRealPlace(EventContext context) {
            return new MultiregattaRegattasPlace(context);
        }
    }
}
