package com.sap.sailing.gwt.home.communication.event;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import com.google.gwt.core.shared.GwtIncompatible;
import com.sap.sailing.gwt.dispatch.client.ResultWithTTL;
import com.sap.sailing.gwt.dispatch.client.SortedSetResult;
import com.sap.sailing.gwt.dispatch.client.caching.IsClientCacheable;
import com.sap.sailing.gwt.home.communication.SailingAction;
import com.sap.sailing.gwt.home.communication.SailingDispatchContext;
import com.sap.sailing.gwt.home.communication.regatta.RegattaWithProgressDTO;
import com.sap.sailing.gwt.home.server.EventActionUtil;
import com.sap.sailing.gwt.home.server.LeaderboardContext;
import com.sap.sailing.gwt.home.server.EventActionUtil.LeaderboardCallback;
import com.sap.sse.common.Duration;

public class GetRegattaListViewAction implements SailingAction<ResultWithTTL<SortedSetResult<RegattaWithProgressDTO>>>,
        IsClientCacheable {
    private UUID eventId;
    
    @SuppressWarnings("unused")
    private GetRegattaListViewAction() {
    }

    public GetRegattaListViewAction(UUID eventId) {
        this.eventId = eventId;
    }

    @Override
    @GwtIncompatible
    public ResultWithTTL<SortedSetResult<RegattaWithProgressDTO>> execute(final SailingDispatchContext context) {
        final List<RegattaWithProgressDTO> result = new LinkedList<>();
        EventActionUtil.forLeaderboardsOfEvent(context, eventId, new LeaderboardCallback() {
            @Override
            public void doForLeaderboard(LeaderboardContext leaderboardContext) {
                result.add(leaderboardContext.getRegattaWithProgress());
            }
        });
        return new ResultWithTTL<>(EventActionUtil.getEventStateDependentTTL(context, eventId,
                Duration.ONE_MINUTE.times(3)), new SortedSetResult<>(result));
    }

    @Override
    public void cacheInstanceKey(StringBuilder key) {
        key.append(eventId);
    }
}
