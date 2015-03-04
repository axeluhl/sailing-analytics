package com.sap.sailing.gwt.home.client.place.event2.regatta.tabs;

import com.sap.sailing.gwt.home.client.place.event2.AbstractEventPlace;
import com.sap.sailing.gwt.home.client.place.event2.EventContext;
import com.sap.sailing.gwt.home.client.place.event2.regatta.AbstractEventRegattaPlace;

public class RegattaRacesPlace extends AbstractEventRegattaPlace {
    public RegattaRacesPlace(String id, String leaderboardName) {
        super(id, leaderboardName);
    }
    
    public RegattaRacesPlace(EventContext context) {
        super(context);
    }

    @Override
    public AbstractEventRegattaPlace newInstanceWithContext(EventContext ctx) {
        return new RegattaRacesPlace(ctx);
    }

    public static class Tokenizer extends AbstractEventPlace.Tokenizer<RegattaRacesPlace> {
        @Override
        protected RegattaRacesPlace getRealPlace(EventContext context) {
            return new RegattaRacesPlace(context);
        }
    }
}
