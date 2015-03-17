package com.sap.sailing.gwt.home.client.place.event2.regatta.tabs;

import com.google.gwt.place.shared.Prefix;
import com.sap.sailing.gwt.home.client.place.event2.AbstractEventPlace;
import com.sap.sailing.gwt.home.client.place.event2.EventContext;
import com.sap.sailing.gwt.home.client.place.event2.EventPrefixes;
import com.sap.sailing.gwt.home.client.place.event2.regatta.AbstractEventRegattaPlace;

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

    @Prefix(EventPrefixes.RegattaCompetitorAnalytics)
    public static class Tokenizer extends AbstractEventPlace.Tokenizer<RegattaCompetitorAnalyticsPlace> {

        @Override
        protected RegattaCompetitorAnalyticsPlace getRealPlace(EventContext context) {
            return new RegattaCompetitorAnalyticsPlace(context);
        }
    }
}
