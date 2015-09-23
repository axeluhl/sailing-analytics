package com.sap.sailing.gwt.home.client.place.event.multiregatta.tabs;

import com.google.gwt.place.shared.Prefix;
import com.sap.sailing.gwt.home.client.place.event.multiregatta.AbstractMultiregattaEventPlace;
import com.sap.sailing.gwt.home.shared.places.PlaceTokenPrefixes;
import com.sap.sailing.gwt.home.shared.places.event.AbstractEventPlace;
import com.sap.sailing.gwt.home.shared.places.event.EventContext;

public class MultiregattaRegattasPlace extends AbstractMultiregattaEventPlace {
    public MultiregattaRegattasPlace(String id) {
        super(id);
    }
    
    public MultiregattaRegattasPlace(EventContext context) {
        super(context);
    }

    @Prefix(PlaceTokenPrefixes.MultiregattaRegattas)
    public static class Tokenizer extends AbstractEventPlace.Tokenizer<MultiregattaRegattasPlace> {
        @Override
        protected MultiregattaRegattasPlace getRealPlace(EventContext context) {
            return new MultiregattaRegattasPlace(context);
        }
    }
}
