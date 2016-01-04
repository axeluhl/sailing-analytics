package com.sap.sailing.gwt.home.desktop.places.event.multiregatta.overviewtab;

import com.google.gwt.place.shared.Prefix;
import com.sap.sailing.gwt.home.desktop.places.event.multiregatta.AbstractMultiregattaEventPlace;
import com.sap.sailing.gwt.home.shared.app.HasMobileVersion;
import com.sap.sailing.gwt.home.shared.places.PlaceTokenPrefixes;
import com.sap.sailing.gwt.home.shared.places.event.AbstractEventPlace;
import com.sap.sailing.gwt.home.shared.places.event.EventContext;

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
