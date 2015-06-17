package com.sap.sailing.gwt.home.client.place.event.regatta.tabs;

import com.google.gwt.place.shared.Prefix;
import com.sap.sailing.gwt.home.client.place.event.AbstractEventPlace;
import com.sap.sailing.gwt.home.client.place.event.EventContext;
import com.sap.sailing.gwt.home.client.place.event.EventPrefixes;
import com.sap.sailing.gwt.home.client.place.event.regatta.AbstractEventRegattaPlace;
import com.sap.sailing.gwt.home.shared.app.HasLocationTitle;
import com.sap.sailing.gwt.home.shared.app.HasMobileVersion;

public class RegattaLeaderboardPlace extends AbstractEventRegattaPlace implements HasLocationTitle, HasMobileVersion {
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
