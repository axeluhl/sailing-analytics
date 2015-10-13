package com.sap.sailing.gwt.ui.shared.dispatch.event;

import java.util.UUID;

import com.google.gwt.core.shared.GwtIncompatible;
import com.sap.sailing.gwt.home.shared.dispatch.IsClientCacheable;
import com.sap.sailing.gwt.ui.shared.dispatch.Action;
import com.sap.sailing.gwt.ui.shared.dispatch.DispatchContext;
import com.sap.sailing.gwt.ui.shared.dispatch.ResultWithTTL;

public class GetMiniLeaderbordAction implements Action<ResultWithTTL<GetMiniLeaderboardDTO>>, IsClientCacheable {
    private UUID eventId;
    private String leaderboardName;
    private int limit = 0;

    @SuppressWarnings("unused")
    private GetMiniLeaderbordAction() {
    }

    public GetMiniLeaderbordAction(UUID eventId, String leaderboardName) {
        this(eventId, leaderboardName, 0);
    }
    
    public GetMiniLeaderbordAction(UUID eventId, String leaderboardName, int limit) {
        this.eventId = eventId;
        this.leaderboardName = leaderboardName;
        this.limit = limit;
    }

    @Override
    @GwtIncompatible
    public ResultWithTTL<GetMiniLeaderboardDTO> execute(DispatchContext context) {
        return EventActionUtil.getLeaderboardContext(context, eventId, leaderboardName).calculateMiniLeaderboard(context.getRacingEventService(), limit);
    }

    @Override
    public void cacheInstanceKey(StringBuilder key) {
        key.append(eventId);
        key.append("_");
        key.append(leaderboardName);
        key.append("_");
        key.append(limit);
    }
}
