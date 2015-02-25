package com.sap.sailing.gwt.home.client.place.event2.regatta;

import com.google.gwt.place.shared.PlaceTokenizer;
import com.sap.sailing.gwt.home.client.place.event2.AbstractEventPlace;
import com.sap.sailing.gwt.home.client.place.event2.tabs.EventContext;

public abstract class AbstractEventRegattaPlace extends AbstractEventPlace {

    public AbstractEventRegattaPlace(EventContext ctx) {
        super(ctx);
    }

    public AbstractEventRegattaPlace(String eventUuidAsString, String regattaId) {
        super(eventUuidAsString);
        getCtx().withRegattaId(regattaId);
    }

    public static abstract class Tokenizer<PLACE extends AbstractEventRegattaPlace> implements PlaceTokenizer<PLACE> {
        @Override
        public String getToken(PLACE place) {
            return place.getEventUuidAsString() + ";" + place.getRegattaId();
        }

        @Override
        public PLACE getPlace(String token) {
            String[] elements = token.split(";");
            return getRealPlace(elements[0], elements[1]);
        }
        
        protected abstract PLACE getRealPlace(String eventId, String regattaId);
    }
}
