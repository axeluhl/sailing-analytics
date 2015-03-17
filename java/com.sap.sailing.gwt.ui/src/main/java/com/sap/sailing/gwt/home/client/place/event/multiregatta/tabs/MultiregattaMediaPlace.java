package com.sap.sailing.gwt.home.client.place.event.multiregatta.tabs;

import com.google.gwt.place.shared.Prefix;
import com.sap.sailing.gwt.home.client.place.event.AbstractEventPlace;
import com.sap.sailing.gwt.home.client.place.event.EventContext;
import com.sap.sailing.gwt.home.client.place.event.EventPrefixes;
import com.sap.sailing.gwt.home.client.place.event.multiregatta.AbstractMultiregattaEventPlace;

public class MultiregattaMediaPlace extends AbstractMultiregattaEventPlace {
    public MultiregattaMediaPlace(String id) {
        super(id);
    }
    
    public MultiregattaMediaPlace(EventContext context) {
        super(context);
    }

    @Prefix(EventPrefixes.MultiregattaMedia)
    public static class Tokenizer extends AbstractEventPlace.Tokenizer<MultiregattaMediaPlace> {
        @Override
        protected MultiregattaMediaPlace getRealPlace(EventContext context) {
            return new MultiregattaMediaPlace(context);
        }
    }
}
