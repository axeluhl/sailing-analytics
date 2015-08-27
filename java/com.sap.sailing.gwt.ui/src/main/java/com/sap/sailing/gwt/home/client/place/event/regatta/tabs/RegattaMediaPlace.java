package com.sap.sailing.gwt.home.client.place.event.regatta.tabs;

import com.google.gwt.place.shared.Prefix;
import com.sap.sailing.gwt.home.client.place.event.AbstractEventPlace;
import com.sap.sailing.gwt.home.client.place.event.EventContext;
import com.sap.sailing.gwt.home.client.place.event.EventPrefixes;
import com.sap.sailing.gwt.home.client.place.event.regatta.AbstractEventRegattaPlace;
import com.sap.sailing.gwt.home.shared.ExperimentalFeatures;
import com.sap.sailing.gwt.home.shared.app.MobileSupport;

public class RegattaMediaPlace extends AbstractEventRegattaPlace implements MobileSupport {
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
    
    @Override
    public boolean hasMobileVersion() {
        return ExperimentalFeatures.SHOW_MEDIA_PAGE_ON_MOBILE;
    }
}
