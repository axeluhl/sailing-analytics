package com.sap.sailing.gwt.home.client.place.event2.regatta.tabs;

import com.sap.sailing.gwt.home.client.place.event2.EventContext;
import com.sap.sailing.gwt.home.client.place.event2.regatta.AbstractEventRegattaPlace;

public class RegattaCompetitorAnalyticsPlace extends AbstractEventRegattaPlace {
    public RegattaCompetitorAnalyticsPlace(String id, String regattaId) {
        super(id, regattaId);
    }

    public RegattaCompetitorAnalyticsPlace(EventContext context) {
        super(context);
    }

    public static class Tokenizer extends AbstractEventRegattaPlace.Tokenizer<RegattaCompetitorAnalyticsPlace> {

        @Override
        protected RegattaCompetitorAnalyticsPlace getRealPlace(String eventId, String regattaId) {
            return new RegattaCompetitorAnalyticsPlace(eventId, regattaId);
        }
    }
}
