package com.sap.sailing.gwt.home.client.place.event2.regatta.tabs;

import com.sap.sailing.gwt.home.client.place.event2.EventContext;
import com.sap.sailing.gwt.home.client.place.event2.regatta.AbstractEventRegattaPlace;

public class RegattaMediaPlace extends AbstractEventRegattaPlace {
    public RegattaMediaPlace(String id, String regattaId) {
        super(id, regattaId);
    }
    
    public RegattaMediaPlace(EventContext context) {
        super(context);
    }

    @Override
    public AbstractEventRegattaPlace newInstanceWithContext(EventContext ctx) {
        return new RegattaMediaPlace(ctx);
    }

    public static class Tokenizer extends AbstractEventRegattaPlace.Tokenizer<RegattaMediaPlace> {

        @Override
        protected RegattaMediaPlace getRealPlace(String eventId, String regattaId) {
            return new RegattaMediaPlace(eventId, regattaId);
        }
    }
}
