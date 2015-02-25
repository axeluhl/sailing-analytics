package com.sap.sailing.gwt.home.client.place.event2.regatta.tabs;

import com.sap.sailing.gwt.home.client.place.event2.EventContext;
import com.sap.sailing.gwt.home.client.place.event2.regatta.AbstractEventRegattaPlace;

public class RegattaLeaderboardPlace extends AbstractEventRegattaPlace {
    public RegattaLeaderboardPlace(String id, String regattaId) {
        super(id, regattaId);
    }
    
    public RegattaLeaderboardPlace(EventContext context) {
        super(context);
    }

    public static class Tokenizer extends AbstractEventRegattaPlace.Tokenizer<RegattaLeaderboardPlace> {
        @Override
        protected RegattaLeaderboardPlace getRealPlace(String eventId, String leaderboardName) {
            return new RegattaLeaderboardPlace(eventId, leaderboardName);
        }
    }
}
