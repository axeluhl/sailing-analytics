package com.sap.sailing.gwt.home.client.place.event.regatta.tabs;

import com.google.gwt.place.shared.Prefix;
import com.sap.sailing.gwt.home.client.place.event.regatta.AbstractEventRegattaPlace;
import com.sap.sailing.gwt.home.shared.app.HasMobileVersion;
import com.sap.sailing.gwt.home.shared.places.PlaceTokenPrefixes;
import com.sap.sailing.gwt.home.shared.places.event.AbstractEventPlace;
import com.sap.sailing.gwt.home.shared.places.event.EventContext;

public class RegattaOverviewPlace extends AbstractEventRegattaPlace implements HasMobileVersion {
    public RegattaOverviewPlace(String id, String regattaId) {
        super(id, regattaId);
    }

    public RegattaOverviewPlace(EventContext context) {
        super(context);
    }

    @Override
    public AbstractEventRegattaPlace newInstanceWithContext(EventContext ctx) {
        return new RegattaOverviewPlace(ctx);
    }

    @Prefix(PlaceTokenPrefixes.RegattaOverview)
    public static class Tokenizer extends AbstractEventPlace.Tokenizer<RegattaOverviewPlace> {

        @Override
        protected RegattaOverviewPlace getRealPlace(EventContext context) {
            return new RegattaOverviewPlace(context);
        }
    }
}
