package com.sap.sailing.gwt.home.client.place.event2.tabs.leaderboard;

import com.google.gwt.place.shared.PlaceTokenizer;
import com.sap.sailing.gwt.home.client.place.event2.regatta.AbstractEventRegattaPlace;
import com.sap.sailing.gwt.home.client.place.event2.tabs.EventContext;

public class EventLeaderboardPlace extends AbstractEventRegattaPlace {
    public EventLeaderboardPlace(String id, String regattaId) {
        super(id, regattaId);
    }
    
    public EventLeaderboardPlace(EventContext context) {
        super(context);
    }

    public static class Tokenizer implements PlaceTokenizer<EventLeaderboardPlace> {
        @Override
        public String getToken(EventLeaderboardPlace place) {
            return place.getEventUuidAsString();
        }

        @Override
        public EventLeaderboardPlace getPlace(String token) {
            // TODO
            return new EventLeaderboardPlace(token, "");
        }
    }
}
