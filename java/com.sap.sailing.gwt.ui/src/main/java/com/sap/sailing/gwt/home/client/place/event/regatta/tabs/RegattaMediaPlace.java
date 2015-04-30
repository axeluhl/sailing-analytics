package com.sap.sailing.gwt.home.client.place.event.regatta.tabs;

import com.google.gwt.place.shared.Prefix;
import com.sap.sailing.gwt.home.client.place.event.AbstractEventPlace;
import com.sap.sailing.gwt.home.client.place.event.EventContext;
import com.sap.sailing.gwt.home.client.place.event.EventPrefixes;
import com.sap.sailing.gwt.home.client.place.event.regatta.AbstractEventRegattaPlace;

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

    @Prefix(EventPrefixes.RegattaMedia)
    public static class Tokenizer extends AbstractEventPlace.Tokenizer<RegattaMediaPlace> {

        @Override
        protected RegattaMediaPlace getRealPlace(EventContext context) {
            return new RegattaMediaPlace(context);
        }
    }
}
