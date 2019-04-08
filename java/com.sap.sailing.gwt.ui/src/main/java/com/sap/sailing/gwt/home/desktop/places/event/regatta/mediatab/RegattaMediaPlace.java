package com.sap.sailing.gwt.home.desktop.places.event.regatta.mediatab;

import com.google.gwt.place.shared.Prefix;
import com.sap.sailing.gwt.home.desktop.places.event.regatta.AbstractEventRegattaPlace;
import com.sap.sailing.gwt.home.shared.app.HasMobileVersion;
import com.sap.sailing.gwt.home.shared.places.PlaceTokenPrefixes;
import com.sap.sailing.gwt.home.shared.places.event.AbstractEventPlace;
import com.sap.sailing.gwt.home.shared.places.event.EventContext;

public class RegattaMediaPlace extends AbstractEventRegattaPlace implements HasMobileVersion {
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

    @Prefix(PlaceTokenPrefixes.RegattaMedia)
    public static class Tokenizer extends AbstractEventPlace.Tokenizer<RegattaMediaPlace> {

        @Override
        protected RegattaMediaPlace getRealPlace(EventContext context) {
            return new RegattaMediaPlace(context);
        }
    }
}
