package com.sap.sailing.gwt.home.shared.places.event;

import java.util.HashMap;
import java.util.Map;

import com.google.gwt.place.shared.Place;
import com.sap.sailing.gwt.common.client.AbstractMapTokenizer;
import com.sap.sailing.gwt.home.shared.app.HasLocationTitle;
import com.sap.sailing.gwt.ui.client.StringMessages;

public abstract class AbstractEventPlace extends Place implements HasLocationTitle {
    private final EventContext ctx;

    protected AbstractEventPlace(EventContext ctx) {
        this.ctx = ctx;
    }

    public EventContext getCtx() {
        return ctx;
    }

    public AbstractEventPlace(String eventUuidAsString) {
        this.ctx = new EventContext();
        ctx.withId(eventUuidAsString);
    }

    public String getTitle(String eventName) {
        return StringMessages.INSTANCE.sapSailing() + " - " + eventName;
    }

    @Override
    public String getLocationTitle() {
        return StringMessages.INSTANCE.events();
    }

    public String getEventUuidAsString() {
        return ctx.getEventId();
    }
    
    public String getRegattaId() {
        return ctx.getRegattaId();
    }
    
    public static abstract class Tokenizer<PLACE extends AbstractEventPlace> extends AbstractMapTokenizer<PLACE> {
        private final static String PARAM_EVENTID = "eventId";
        private final static String PARAM_REGATTAID = "regattaId";
        protected PLACE getPlaceFromParameters(Map<String, String> parameters) {
            return getRealPlace(new EventContext().withId(parameters.get(PARAM_EVENTID)).withRegattaId(parameters.get(PARAM_REGATTAID)));
        }
        
        protected Map<String, String> getParameters(PLACE place) {
            Map<String, String> parameters = new HashMap<>();
            EventContext context = place.getCtx();
            parameters.put(PARAM_EVENTID, context.getEventId());
            String regattaId = context.getRegattaId();
            if(regattaId != null && !regattaId.isEmpty()) {
                parameters.put(PARAM_REGATTAID, context.getRegattaId());
            }
            return parameters;
        }
        
        protected abstract PLACE getRealPlace(EventContext context);
    }
}
