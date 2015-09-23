package com.sap.sailing.gwt.home.shared.places.event;

import com.google.gwt.place.shared.Prefix;
import com.sap.sailing.gwt.home.shared.app.HasMobileVersion;
import com.sap.sailing.gwt.home.shared.places.PlaceTokenPrefixes;

public class EventDefaultPlace extends AbstractEventPlace implements HasMobileVersion {

    public EventDefaultPlace(EventContext ctx) {
        super(ctx);
    }

    public EventDefaultPlace(String eventUuidAsString) {
        super(eventUuidAsString);
    }

    @Prefix(PlaceTokenPrefixes.EventDefault)
    public static class Tokenizer extends AbstractEventPlace.Tokenizer<EventDefaultPlace> {
        @Override
        protected EventDefaultPlace getRealPlace(EventContext context) {
            return new EventDefaultPlace(context);
        }
    }
}
