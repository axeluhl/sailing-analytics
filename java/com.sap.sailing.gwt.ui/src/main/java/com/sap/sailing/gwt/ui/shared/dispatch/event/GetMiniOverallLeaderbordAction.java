package com.sap.sailing.gwt.ui.shared.dispatch.event;

import java.util.UUID;

import com.google.gwt.core.shared.GwtIncompatible;
import com.sap.sailing.gwt.home.shared.dispatch.IsClientCacheable;
import com.sap.sailing.gwt.ui.shared.dispatch.Action;
import com.sap.sailing.gwt.ui.shared.dispatch.DispatchContext;
import com.sap.sailing.gwt.ui.shared.dispatch.ResultWithTTL;

public class GetMiniOverallLeaderbordAction implements Action<ResultWithTTL<GetMiniLeaderboardDTO>>, IsClientCacheable {
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
    public ResultWithTTL<GetMiniLeaderboardDTO> execute(DispatchContext context) {
        return EventActionUtil.getOverallLeaderboardContext(context, seriesId).calculateMiniLeaderboard(context.getRacingEventService(), limit);
    }

    @Override
    public void cacheInstanceKey(StringBuilder key) {
        key.append(seriesId).append("_").append(limit);
    }
}
