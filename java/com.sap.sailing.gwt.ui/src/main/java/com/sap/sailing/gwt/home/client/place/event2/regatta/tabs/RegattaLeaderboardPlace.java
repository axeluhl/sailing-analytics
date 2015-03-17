package com.sap.sailing.gwt.home.client.place.event2.regatta.tabs;

import com.google.gwt.place.shared.Prefix;
import com.sap.sailing.gwt.home.client.place.event2.AbstractEventPlace;
import com.sap.sailing.gwt.home.client.place.event2.EventContext;
import com.sap.sailing.gwt.home.client.place.event2.EventPrefixes;
import com.sap.sailing.gwt.home.client.place.event2.regatta.AbstractEventRegattaPlace;

public class RegattaLeaderboardPlace extends AbstractEventRegattaPlace {
    public RegattaLeaderboardPlace(String id, String regattaId) {
        super(id, regattaId);
    }
    
    public RegattaLeaderboardPlace(EventContext context) {
        super(context);
    }

    @Override
    public AbstractEventRegattaPlace newInstanceWithContext(EventContext ctx) {
        return new RegattaLeaderboardPlace(ctx);
    }

    @Prefix(EventPrefixes.RegattaLeaderboard)
    public static class Tokenizer extends AbstractEventPlace.Tokenizer<RegattaLeaderboardPlace> {
        @Override
        protected RegattaLeaderboardPlace getRealPlace(EventContext context) {
            return new RegattaLeaderboardPlace(context);
        }
    }
}
