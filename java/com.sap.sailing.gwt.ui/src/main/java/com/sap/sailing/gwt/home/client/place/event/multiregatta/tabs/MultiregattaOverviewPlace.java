package com.sap.sailing.gwt.home.client.place.event.multiregatta.tabs;

import com.google.gwt.place.shared.Prefix;
import com.sap.sailing.gwt.home.client.place.event.AbstractEventPlace;
import com.sap.sailing.gwt.home.client.place.event.EventContext;
import com.sap.sailing.gwt.home.client.place.event.multiregatta.AbstractMultiregattaEventPlace;
import com.sap.sailing.gwt.home.shared.app.HasMobileVersion;
import com.sap.sailing.gwt.home.shared.places.PlaceTokenPrefixes;

public class MultiregattaOverviewPlace extends AbstractMultiregattaEventPlace implements HasMobileVersion {
    public MultiregattaOverviewPlace(String id) {
        super(id);
    }
    
    public MultiregattaOverviewPlace(EventContext context) {
        super(context);
    }

    @Prefix(PlaceTokenPrefixes.MultiregattaOverview)
    public static class Tokenizer extends AbstractEventPlace.Tokenizer<MultiregattaOverviewPlace> {
        @Override
        protected MultiregattaOverviewPlace getRealPlace(EventContext context) {
            return new MultiregattaOverviewPlace(context);
        }
    }
}
