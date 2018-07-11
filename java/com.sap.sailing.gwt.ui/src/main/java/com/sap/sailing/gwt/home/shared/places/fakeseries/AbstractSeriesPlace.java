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
            String eventIdRaw = parameters.get(PARAM_EVENTID);
            UUID eventId = null;
            if (eventIdRaw != null) {
                eventId = UUID.fromString(eventIdRaw);
            }
            String leaderboardGroupIdRaw = parameters.get(PARAM_LEADERBOARD_GROUP_UUID);
            UUID leaderboardGroupId = null;
            if(leaderboardGroupIdRaw != null) {
                leaderboardGroupId = UUID.fromString(leaderboardGroupIdRaw);
            }
            return getRealPlace(new SeriesContext(eventId, leaderboardGroupId));
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
