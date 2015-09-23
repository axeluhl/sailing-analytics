package com.sap.sailing.gwt.home.client.place.event.regatta.tabs;

import com.google.gwt.place.shared.Prefix;
import com.sap.sailing.gwt.home.client.place.event.AbstractEventPlace;
import com.sap.sailing.gwt.home.client.place.event.EventContext;
import com.sap.sailing.gwt.home.client.place.event.regatta.AbstractEventRegattaPlace;
import com.sap.sailing.gwt.home.shared.places.PlaceTokenPrefixes;

public class RegattaCompetitorAnalyticsPlace extends AbstractEventRegattaPlace {
    public RegattaCompetitorAnalyticsPlace(String id, String regattaId) {
        super(id, regattaId);
    }

    public RegattaCompetitorAnalyticsPlace(EventContext context) {
        super(context);
    }
    
    @Override
    public AbstractEventRegattaPlace newInstanceWithContext(EventContext ctx) {
        return new RegattaCompetitorAnalyticsPlace(ctx);
    }

    @Prefix(PlaceTokenPrefixes.RegattaCompetitorAnalytics)
    public static class Tokenizer extends AbstractEventPlace.Tokenizer<RegattaCompetitorAnalyticsPlace> {

        @Override
        protected RegattaCompetitorAnalyticsPlace getRealPlace(EventContext context) {
            return new RegattaCompetitorAnalyticsPlace(context);
        }
    }
}
