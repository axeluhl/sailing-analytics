package com.sap.sailing.gwt.home.communication.event.minileaderboard;

import java.util.UUID;

import com.google.gwt.core.shared.GwtIncompatible;
import com.sap.sailing.gwt.dispatch.client.ResultWithTTL;
import com.sap.sailing.gwt.dispatch.client.caching.IsClientCacheable;
import com.sap.sailing.gwt.home.communication.SailingAction;
import com.sap.sailing.gwt.home.communication.SailingDispatchContext;
import com.sap.sailing.gwt.home.server.EventActionUtil;

public class GetMiniOverallLeaderbordAction implements SailingAction<ResultWithTTL<GetMiniLeaderboardDTO>>, IsClientCacheable {
    private UUID seriesId;
    private int limit = 0;

    @SuppressWarnings("unused")
    private GetMiniOverallLeaderbordAction() {
    }

    public GetMiniOverallLeaderbordAction(UUID eventId) {
        this(eventId, 0);
    }
    
    public GetMiniOverallLeaderbordAction(UUID seriesId, int limit) {
        this.seriesId = seriesId;
        this.limit = limit;
    }

    @Override
    @GwtIncompatible
    public ResultWithTTL<GetMiniLeaderboardDTO> execute(SailingDispatchContext context) {
        return EventActionUtil.getOverallLeaderboardContext(context, seriesId).calculateMiniLeaderboard(context.getRacingEventService(), limit);
    }

    @Override
    public void cacheInstanceKey(StringBuilder key) {
        key.append(seriesId).append("_").append(limit);
    }
}
