package com.sap.sailing.gwt.home.shared.places.fakeseries;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

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
        UUID asUUID = UUID.fromString(eventUuidAsString);
        this.ctx = SeriesContext.createWithSeriesId(asUUID);
    }

    public String getTitle(String eventName) {
        return StringMessages.INSTANCE.sapSailing() + " - " + eventName;
    }

    public String getSeriesUuidAsString() {
        return ctx.getSeriesId().toString();
    }
    
    public static abstract class Tokenizer<PLACE extends AbstractSeriesPlace> extends AbstractMapTokenizer<PLACE> {
        private final static String PARAM_EVENTID = "seriesId";
	private final static String PARAM_LEADERBOARD_GROUP_UUID = "leaderboardGroupId";
       
        protected PLACE getPlaceFromParameters(Map<String, Set<String>> parameters) {
            String leaderboardGroupIdRaw = Util.first(parameters.get(PARAM_LEADERBOARD_GROUP_UUID));
            SeriesContext ctx;
            if (leaderboardGroupIdRaw != null) {
                ctx = SeriesContext.createWithLeaderboardGroupId(UUID.fromString(leaderboardGroupIdRaw));
            } else {
                String eventIdRaw = Util.first(parameters.get(PARAM_EVENTID));
                ctx = SeriesContext.createWithSeriesId(UUID.fromString(eventIdRaw));
            }
            return getRealPlace(ctx);
        }
        
        protected Map<String, Set<String>> getParameters(PLACE place) {
            Map<String, Set<String>> parameters = new HashMap<>();
            SeriesContext context = place.getCtx();
            if(context.getLeaderboardGroupId() != null) {
               Util.addToValueSet(parameters, PARAM_LEADERBOARD_GROUP_UUID, context.getLeaderboardGroupId().toString());
            }
            //fallback only generate old urls if not possible otherwise!
            if(context.getLeaderboardGroupId() == null && context.getSeriesId() != null) {
                Util.addToValueSet(parameters, PARAM_EVENTID, context.getSeriesId().toString());
            }
            return parameters;
        }
        
        protected abstract PLACE getRealPlace(SeriesContext context);
    }
}
