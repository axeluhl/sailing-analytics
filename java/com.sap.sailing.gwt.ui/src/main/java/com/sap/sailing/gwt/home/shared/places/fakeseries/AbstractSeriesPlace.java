package com.sap.sailing.gwt.home.shared.places.fakeseries;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.google.gwt.place.shared.Place;
import com.sap.sailing.gwt.common.client.AbstractMapTokenizer;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.common.Util;

public abstract class AbstractSeriesPlace extends Place {
    private final SeriesContext ctx;

    protected AbstractSeriesPlace(SeriesContext ctx) {
        this.ctx = ctx;
    }

    public SeriesContext getCtx() {
        return ctx;
    }

    public AbstractSeriesPlace(String eventUuidAsString) {
        this.ctx = new SeriesContext(eventUuidAsString);
    }

    public String getTitle(String eventName) {
        return StringMessages.INSTANCE.sapSailing() + " - " + eventName;
    }

    public String getSeriesUuidAsString() {
        return ctx.getSeriesId();
    }
    
    public static abstract class Tokenizer<PLACE extends AbstractSeriesPlace> extends AbstractMapTokenizer<PLACE> {
        private final static String PARAM_EVENTID = "seriesId";

        protected PLACE getPlaceFromParameters(Map<String, Set<String>> parameters) {
            return getRealPlace(new SeriesContext(parameters.get(PARAM_EVENTID).stream().findFirst().orElse("")));
        }
        
        protected Map<String, Set<String>> getParameters(PLACE place) {
            Map<String, Set<String>> parameters = new HashMap<>();
            SeriesContext context = place.getCtx();
            Util.addToValueSet(parameters, PARAM_EVENTID, context.getSeriesId());
            return parameters;
        }
        
        protected abstract PLACE getRealPlace(SeriesContext context);
    }
}
