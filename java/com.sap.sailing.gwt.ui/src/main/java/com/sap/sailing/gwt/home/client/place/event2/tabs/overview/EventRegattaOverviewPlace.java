package com.sap.sailing.gwt.home.client.place.event2.tabs.overview;

import com.sap.sailing.gwt.home.client.place.event2.regatta.AbstractEventRegattaPlace;
import com.sap.sailing.gwt.home.client.place.event2.tabs.EventContext;

public class EventRegattaOverviewPlace extends AbstractEventRegattaPlace {
    public EventRegattaOverviewPlace(String id, String regattaId) {
        super(id, regattaId);
    }
    
    public EventRegattaOverviewPlace(EventContext context) {
        super(context);
    }

    public static class Tokenizer extends AbstractEventRegattaPlace.Tokenizer<EventRegattaOverviewPlace> {
        @Override
        protected EventRegattaOverviewPlace getRealPlace(String eventId, String leaderboardName) {
            return new EventRegattaOverviewPlace(eventId, leaderboardName);
        }
    }
}
