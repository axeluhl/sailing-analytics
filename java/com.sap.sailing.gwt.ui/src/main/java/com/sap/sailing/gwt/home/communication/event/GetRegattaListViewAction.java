package com.sap.sailing.gwt.home.communication.event;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import com.google.gwt.core.shared.GwtIncompatible;
import com.sap.sailing.gwt.home.communication.SailingAction;
import com.sap.sailing.gwt.home.communication.SailingDispatchContext;
import com.sap.sailing.gwt.home.communication.regatta.RegattaWithProgressDTO;
import com.sap.sailing.gwt.home.server.EventActionUtil;
import com.sap.sailing.gwt.home.server.EventActionUtil.LeaderboardCallback;
import com.sap.sailing.gwt.home.server.LeaderboardContext;
import com.sap.sse.common.Duration;
import com.sap.sse.gwt.dispatch.shared.caching.IsClientCacheable;
import com.sap.sse.gwt.dispatch.shared.commands.ResultWithTTL;
import com.sap.sse.gwt.dispatch.shared.commands.SortedSetResult;

/**
 * <p>
 * {@link SailingAction} implementation to load data to be shown on the event overview or regattas page for the
 * {@link #GetRegattaListViewAction(UUID) given event-id}, preparing the appropriate data structure.
 * </p>
 * <p>
 * The {@link ResultWithTTL result's} time to live
 * {@link EventActionUtil#getEventStateDependentTTL(SailingDispatchContext, UUID, Duration) depends on the event's
 * state} using a duration of <i>3 minutes</i> for currently running events.
 * </p>
 */
public class GetRegattaListViewAction implements SailingAction<ResultWithTTL<SortedSetResult<RegattaWithProgressDTO>>>,
        IsClientCacheable {
    private UUID eventId;
    
    @SuppressWarnings("unused")
    private GetRegattaListViewAction() {
    }

    /**
     * Creates a {@link GetRegattaListViewAction} instance for the given event-id.
     * 
     * @param eventId
     *            {@link UUID} of the event to load regattas for
     */
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
        // The following will reduce multiple occurrences of the same regatta, e.g., in different leaderboard groups,
        // to one occurrence only, randomly picked, by means of the Comparable property of RegattaWithProgressDTO.
        return new ResultWithTTL<>(EventActionUtil.getEventStateDependentTTL(context, eventId,
                Duration.ONE_MINUTE.times(3)), new SortedSetResult<>(result));
    }

    @Override
    public void cacheInstanceKey(StringBuilder key) {
        key.append(eventId);
    }
}
