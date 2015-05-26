package com.sap.sailing.gwt.home.client.place.event.multiregatta.tabs;

import com.google.gwt.place.shared.Prefix;
import com.sap.sailing.gwt.home.client.place.event.AbstractEventPlace;
import com.sap.sailing.gwt.home.client.place.event.EventContext;
import com.sap.sailing.gwt.home.client.place.event.EventPrefixes;
import com.sap.sailing.gwt.home.client.place.event.multiregatta.AbstractMultiregattaEventPlace;

public class MultiregattaOverviewPlace extends AbstractMultiregattaEventPlace {
    public MultiregattaOverviewPlace(String id) {
        super(id);
    }
    
    public MultiregattaOverviewPlace(EventContext context) {
        super(context);
    }

    @Prefix(EventPrefixes.MultiregattaOverview)
    public static class Tokenizer extends AbstractEventPlace.Tokenizer<MultiregattaOverviewPlace> {
        @Override
        protected MultiregattaOverviewPlace getRealPlace(EventContext context) {
            return new MultiregattaOverviewPlace(context);
        }
    }
}
