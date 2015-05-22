package com.sap.sailing.gwt.home.client.place.fakeseries;

import java.util.HashMap;
import java.util.Map;

import com.google.gwt.place.shared.Place;
import com.sap.sailing.gwt.common.client.AbstractMapTokenizer;
import com.sap.sailing.gwt.common.client.i18n.TextMessages;

public abstract class AbstractSeriesPlace extends Place {
    private final SeriesContext ctx;

    protected AbstractSeriesPlace(SeriesContext ctx) {
        this.ctx = ctx;
    }

    public SeriesContext getCtx() {
        return ctx;
    }

    public AbstractSeriesPlace(String eventUuidAsString) {
        this.ctx = new SeriesContext();
        ctx.withId(eventUuidAsString);
    }

    public String getTitle(String eventName) {
        return TextMessages.INSTANCE.sapSailing() + " - " + eventName;
    }

    public String getSeriesUuidAsString() {
        return ctx.getSeriesId();
    }
    
    public static abstract class Tokenizer<PLACE extends AbstractSeriesPlace> extends AbstractMapTokenizer<PLACE> {
        private final static String PARAM_EVENTID = "seriesId";
        protected PLACE getPlaceFromParameters(Map<String, String> parameters) {
            return getRealPlace(new SeriesContext().withId(parameters.get(PARAM_EVENTID)));
        }
        
        protected Map<String, String> getParameters(PLACE place) {
            Map<String, String> parameters = new HashMap<>();
            SeriesContext context = place.getCtx();
            parameters.put(PARAM_EVENTID, context.getSeriesId());
            return parameters;
        }
        
        protected abstract PLACE getRealPlace(SeriesContext context);
    }
}
