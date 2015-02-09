package com.sap.sailing.gwt.home.client.place.event2.tabs.leaderboard;

import com.google.gwt.place.shared.PlaceTokenizer;
import com.sap.sailing.gwt.home.client.place.event2.AbstractEventPlace;
import com.sap.sailing.gwt.home.client.place.event2.tabs.EventContext;

public class EventLeaderboardPlace extends AbstractEventPlace {
    public EventLeaderboardPlace(String id) {
        super(id);
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
            return new EventLeaderboardPlace(token);
        }
    }
}
