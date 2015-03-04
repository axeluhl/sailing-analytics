package com.sap.sailing.gwt.home.client.place.event2.multiregatta.tabs;

import com.sap.sailing.gwt.home.client.place.event2.AbstractEventPlace;
import com.sap.sailing.gwt.home.client.place.event2.EventContext;
import com.sap.sailing.gwt.home.client.place.event2.multiregatta.AbstractMultiregattaEventPlace;

public class MultiregattaOverviewPlace extends AbstractMultiregattaEventPlace {
    public MultiregattaOverviewPlace(String id) {
        super(id);
    }
    
    public MultiregattaOverviewPlace(EventContext context) {
        super(context);
    }

    public static class Tokenizer extends AbstractEventPlace.Tokenizer<MultiregattaOverviewPlace> {
        @Override
        protected MultiregattaOverviewPlace getRealPlace(EventContext context) {
            return new MultiregattaOverviewPlace(context);
        }
    }
}
