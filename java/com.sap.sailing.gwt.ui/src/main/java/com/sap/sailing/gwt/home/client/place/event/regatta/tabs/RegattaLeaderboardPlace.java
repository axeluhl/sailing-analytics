package com.sap.sailing.gwt.home.client.place.event.regatta.tabs;

import com.google.gwt.place.shared.Prefix;
import com.sap.sailing.gwt.home.client.place.event.AbstractEventPlace;
import com.sap.sailing.gwt.home.client.place.event.EventContext;
import com.sap.sailing.gwt.home.client.place.event.regatta.AbstractEventRegattaPlace;
import com.sap.sailing.gwt.home.shared.places.PlaceTokenPrefixes;

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

    @Prefix(PlaceTokenPrefixes.RegattaLeaderboard)
    public static class Tokenizer extends AbstractEventPlace.Tokenizer<RegattaLeaderboardPlace> {
        @Override
        protected RegattaLeaderboardPlace getRealPlace(EventContext context) {
            return new RegattaLeaderboardPlace(context);
        }
    }
}
