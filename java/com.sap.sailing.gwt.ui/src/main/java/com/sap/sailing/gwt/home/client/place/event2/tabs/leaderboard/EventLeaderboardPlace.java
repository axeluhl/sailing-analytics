package com.sap.sailing.gwt.home.client.place.event2.tabs.leaderboard;

import com.sap.sailing.gwt.home.client.place.event2.regatta.AbstractEventRegattaPlace;
import com.sap.sailing.gwt.home.client.place.event2.tabs.EventContext;

public class EventLeaderboardPlace extends AbstractEventRegattaPlace {
    public EventLeaderboardPlace(String id, String regattaId) {
        super(id, regattaId);
    }
    
    public EventLeaderboardPlace(EventContext context) {
        super(context);
    }

    public static class Tokenizer extends AbstractEventRegattaPlace.Tokenizer<EventLeaderboardPlace> {
        @Override
        protected EventLeaderboardPlace getRealPlace(String eventId, String leaderboardName) {
            return new EventLeaderboardPlace(eventId, leaderboardName);
        }
    }
}
