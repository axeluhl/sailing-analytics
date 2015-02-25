package com.sap.sailing.gwt.home.client.place.event2.regatta.tabs.media;

import com.sap.sailing.gwt.home.client.place.event2.EventContext;
import com.sap.sailing.gwt.home.client.place.event2.regatta.AbstractEventRegattaPlace;

public class MediaPlace extends AbstractEventRegattaPlace {
    public MediaPlace(String id, String regattaId) {
        super(id, regattaId);
    }
    
    public MediaPlace(EventContext context) {
        super(context);
    }

    public static class Tokenizer extends AbstractEventRegattaPlace.Tokenizer<MediaPlace> {

        @Override
        protected MediaPlace getRealPlace(String eventId, String regattaId) {
            return new MediaPlace(eventId, regattaId);
        }
    }
}
