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

    public AbstractSeriesPlace(UUID leaderboardGroupUUID) {
        this.ctx = new SeriesContext(null, leaderboardGroupUUID);
    }

    public String getTitle(String eventName) {
        return StringMessages.INSTANCE.sapSailing() + " - " + eventName;
    }

    public String getSeriesUuidAsString() {
        return ctx.getSeriesId().toString();
    }
    
    public static abstract class Tokenizer<PLACE extends AbstractSeriesPlace> extends AbstractMapTokenizer<PLACE> {
        private final static String PARAM_EVENTID = "seriesId";
        private final static String PARAM_LEADERBOARD_GROUP_NAME = "leaderboardGroupName";
        protected PLACE getPlaceFromParameters(Map<String, String> parameters) {
            String eventIdRaw = parameters.get(PARAM_EVENTID);
            UUID eventId = null;
            if (eventIdRaw != null) {
                eventId = UUID.fromString(eventIdRaw);
            }
            String leaderboardGroupIdRaw = parameters.get(PARAM_LEADERBOARD_GROUP_NAME);
            UUID leaderboardGroupId = null;
            if(leaderboardGroupIdRaw != null) {
                leaderboardGroupId = UUID.fromString(leaderboardGroupIdRaw);
            }
            return getRealPlace(new SeriesContext(eventId, leaderboardGroupId));
        }
        
        protected Map<String, String> getParameters(PLACE place) {
            Map<String, String> parameters = new HashMap<>();
            SeriesContext context = place.getCtx();
            parameters.put(PARAM_LEADERBOARD_GROUP_NAME, context.getLeaderboardGroupName().toString());
            return parameters;
        }
        
        protected abstract PLACE getRealPlace(SeriesContext context);
    }
}
