package com.sap.sailing.gwt.home.client.place.event2.regatta.tabs.overview;

import com.sap.sailing.gwt.home.client.place.event2.EventContext;
import com.sap.sailing.gwt.home.client.place.event2.regatta.AbstractEventRegattaPlace;

public class RegattaOverviewPlace extends AbstractEventRegattaPlace {
    public RegattaOverviewPlace(String id, String regattaId) {
        super(id, regattaId);
    }

    public RegattaOverviewPlace(EventContext context) {
        super(context);
    }

    public static class Tokenizer extends AbstractEventRegattaPlace.Tokenizer<RegattaOverviewPlace> {

        @Override
        protected RegattaOverviewPlace getRealPlace(String eventId, String regattaId) {
            return new RegattaOverviewPlace(eventId, regattaId);
        }
    }
}
