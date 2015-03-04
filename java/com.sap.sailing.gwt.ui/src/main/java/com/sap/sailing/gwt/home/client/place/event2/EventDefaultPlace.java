package com.sap.sailing.gwt.home.client.place.event2;


public class EventDefaultPlace extends AbstractEventPlace {

    public EventDefaultPlace(EventContext ctx) {
        super(ctx);
    }

    public EventDefaultPlace(String eventUuidAsString) {
        super(eventUuidAsString);
    }

    public static class Tokenizer extends AbstractEventPlace.Tokenizer<EventDefaultPlace> {
        @Override
        protected EventDefaultPlace getRealPlace(EventContext context) {
            return new EventDefaultPlace(context);
        }
    }
}
