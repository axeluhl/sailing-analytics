package com.sap.sailing.gwt.home.shared.places.fakeseries;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.google.gwt.place.shared.Place;
import com.sap.sailing.gwt.common.client.AbstractMapTokenizer;
import com.sap.sailing.gwt.ui.client.StringMessages;

public abstract class AbstractSeriesPlace extends Place {
    private final SeriesContext ctx;

    protected AbstractSeriesPlace(SeriesContext ctx) {
        this.ctx = ctx;
    }

    public SeriesContext getCtx() {
        return ctx;
    }

    public String getTitle(String eventName) {
        return StringMessages.INSTANCE.sapSailing() + " - " + eventName;
    }

    public static abstract class Tokenizer<PLACE extends AbstractSeriesPlace> extends AbstractMapTokenizer<PLACE> {
        private final static String PARAM_EVENTID = "seriesId";
        private final static String PARAM_LEADERBOARD_GROUP_UUID = "leaderboardGroupId";
        protected PLACE getPlaceFromParameters(Map<String, String> parameters) {
            String leaderboardGroupIdRaw = parameters.get(PARAM_LEADERBOARD_GROUP_UUID);
            SeriesContext ctx;
            if (leaderboardGroupIdRaw != null) {
                ctx = SeriesContext.createWithLeaderboardGroupId(UUID.fromString(leaderboardGroupIdRaw));
            } else {
                String eventIdRaw = parameters.get(PARAM_EVENTID);
                ctx = SeriesContext.createWithSeriesId(UUID.fromString(eventIdRaw));
            }
            return getRealPlace(ctx);
        }
        
        protected Map<String, String> getParameters(PLACE place) {
            Map<String, String> parameters = new HashMap<>();
            SeriesContext context = place.getCtx();
            if(context.getLeaderboardGroupId() != null) {
                parameters.put(PARAM_LEADERBOARD_GROUP_UUID, context.getLeaderboardGroupId().toString());
            }
            //fallback only generate old urls if not possible otherwise!
            if(context.getLeaderboardGroupId() == null && context.getSeriesId() != null) {
                parameters.put(PARAM_EVENTID, context.getSeriesId().toString());
            }
            return parameters;
        }
        
        protected abstract PLACE getRealPlace(SeriesContext context);
    }
}
