package com.sap.sailing.gwt.home.communication.event.minileaderboard;

import java.util.UUID;

import com.google.gwt.core.shared.GwtIncompatible;
import com.sap.sailing.gwt.dispatch.client.ResultWithTTL;
import com.sap.sailing.gwt.dispatch.client.caching.IsClientCacheable;
import com.sap.sailing.gwt.home.communication.SailingAction;
import com.sap.sailing.gwt.home.communication.SailingDispatchContext;
import com.sap.sailing.gwt.home.server.EventActionUtil;

public class GetMiniLeaderbordAction implements SailingAction<ResultWithTTL<GetMiniLeaderboardDTO>>, IsClientCacheable {
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
    public ResultWithTTL<GetMiniLeaderboardDTO> execute(SailingDispatchContext context) {
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
