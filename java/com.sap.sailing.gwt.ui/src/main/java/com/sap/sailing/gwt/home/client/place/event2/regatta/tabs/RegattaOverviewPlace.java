package com.sap.sailing.gwt.home.client.place.event2.regatta.tabs;

import com.google.gwt.place.shared.Prefix;
import com.sap.sailing.gwt.home.client.place.event2.AbstractEventPlace;
import com.sap.sailing.gwt.home.client.place.event2.EventContext;
import com.sap.sailing.gwt.home.client.place.event2.EventPrefixes;
import com.sap.sailing.gwt.home.client.place.event2.regatta.AbstractEventRegattaPlace;

public class RegattaOverviewPlace extends AbstractEventRegattaPlace {
    public RegattaOverviewPlace(String id, String regattaId) {
        super(id, regattaId);
    }

    public RegattaOverviewPlace(EventContext context) {
        super(context);
    }

    @Override
    public AbstractEventRegattaPlace newInstanceWithContext(EventContext ctx) {
        return new RegattaOverviewPlace(ctx);
    }

    @Prefix(EventPrefixes.RegattaOverview)
    public static class Tokenizer extends AbstractEventPlace.Tokenizer<RegattaOverviewPlace> {

        @Override
        protected RegattaOverviewPlace getRealPlace(EventContext context) {
            return new RegattaOverviewPlace(context);
        }
    }
}
