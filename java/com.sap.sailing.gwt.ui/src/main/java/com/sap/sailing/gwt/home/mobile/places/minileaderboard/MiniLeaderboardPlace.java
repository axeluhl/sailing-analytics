package com.sap.sailing.gwt.home.mobile.places.minileaderboard;

import com.google.gwt.place.shared.Prefix;
import com.sap.sailing.gwt.home.client.place.event.AbstractEventPlace;
import com.sap.sailing.gwt.home.client.place.event.EventContext;
import com.sap.sailing.gwt.home.client.place.event.EventPrefixes;
import com.sap.sailing.gwt.home.client.place.event.regatta.AbstractEventRegattaPlace;
import com.sap.sailing.gwt.home.shared.app.HasLocationTitle;
import com.sap.sailing.gwt.home.shared.app.HasMobileVersion;

public class MiniLeaderboardPlace extends AbstractEventRegattaPlace implements HasLocationTitle, HasMobileVersion {
    public MiniLeaderboardPlace(String id, String regattaId) {
        super(id, regattaId);
    }
    
    public MiniLeaderboardPlace(EventContext context) {
        super(context);
    }

    @Override
    public AbstractEventRegattaPlace newInstanceWithContext(EventContext ctx) {
        return new MiniLeaderboardPlace(ctx);
    }

    @Prefix(EventPrefixes.RegattaMiniLeaderboard)
    public static class Tokenizer extends AbstractEventPlace.Tokenizer<MiniLeaderboardPlace> {
        @Override
        protected MiniLeaderboardPlace getRealPlace(EventContext context) {
            return new MiniLeaderboardPlace(context);
        }
    }
}
