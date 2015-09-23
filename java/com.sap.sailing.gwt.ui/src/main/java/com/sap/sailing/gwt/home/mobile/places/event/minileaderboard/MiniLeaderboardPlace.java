package com.sap.sailing.gwt.home.mobile.places.event.minileaderboard;

import com.google.gwt.place.shared.Prefix;
import com.sap.sailing.gwt.home.desktop.places.event.regatta.AbstractEventRegattaPlace;
import com.sap.sailing.gwt.home.shared.app.HasLocationTitle;
import com.sap.sailing.gwt.home.shared.app.HasMobileVersion;
import com.sap.sailing.gwt.home.shared.places.PlaceTokenPrefixes;
import com.sap.sailing.gwt.home.shared.places.event.AbstractEventPlace;
import com.sap.sailing.gwt.home.shared.places.event.EventContext;

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

    @Prefix(PlaceTokenPrefixes.RegattaMiniLeaderboard)
    public static class Tokenizer extends AbstractEventPlace.Tokenizer<MiniLeaderboardPlace> {
        @Override
        protected MiniLeaderboardPlace getRealPlace(EventContext context) {
            return new MiniLeaderboardPlace(context);
        }
    }
}
