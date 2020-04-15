package com.sap.sailing.gwt.home.shared.places.event;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.google.gwt.place.shared.Place;
import com.sap.sailing.gwt.common.client.AbstractMapTokenizer;
import com.sap.sailing.gwt.home.shared.app.HasLocationTitle;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.common.Util;

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

        protected PLACE getPlaceFromParameters(Map<String, Set<String>> parameters) {
            return getRealPlace(
                    new EventContext().withId(extractSingleParameter(parameters, PARAM_EVENTID))
                            .withRegattaId(extractSingleParameter(parameters, PARAM_REGATTAID)),
                    parameters);
        }

        private String extractSingleParameter(Map<String, Set<String>> parameters, String key) {
            Set<String> param = parameters.get(key);
            return param == null ? null : param.stream().findFirst().orElse(null);
        }

        protected Map<String, Set<String>> getParameters(PLACE place) {
            Map<String, Set<String>> parameters = new HashMap<>();
            EventContext context = place.getCtx();
            Util.addToValueSet(parameters, PARAM_EVENTID, context.getEventId());
            String regattaId = context.getRegattaId();
            if (regattaId != null && !regattaId.isEmpty()) {
                Util.addToValueSet(parameters, PARAM_REGATTAID, context.getRegattaId());
            }
            return parameters;
        }

        protected PLACE getRealPlace(EventContext context, Map<String, Set<String>> parameters) {
            return getRealPlace(context);
        }

        protected abstract PLACE getRealPlace(EventContext context);
    }
}
