package com.sap.sailing.gwt.ui.shared.dispatch.event;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import com.google.gwt.core.shared.GwtIncompatible;
import com.sap.sailing.gwt.ui.shared.dispatch.Action;
import com.sap.sailing.gwt.ui.shared.dispatch.DispatchContext;
import com.sap.sailing.gwt.ui.shared.dispatch.ResultWithTTL;
import com.sap.sailing.gwt.ui.shared.dispatch.SortedSetResult;
import com.sap.sailing.gwt.ui.shared.dispatch.event.EventActionUtil.LeaderboardCallback;
import com.sap.sailing.gwt.ui.shared.dispatch.regatta.RegattaWithProgressDTO;

public class GetRegattaListViewAction implements Action<ResultWithTTL<SortedSetResult<RegattaWithProgressDTO>>> {
    private UUID eventId;
    
    @SuppressWarnings("unused")
    private GetRegattaListViewAction() {
    }

    public GetRegattaListViewAction(UUID eventId) {
        this.eventId = eventId;
    }

    @Override
    @GwtIncompatible
    public ResultWithTTL<SortedSetResult<RegattaWithProgressDTO>> execute(final DispatchContext context) {
        final List<RegattaWithProgressDTO> result = new LinkedList<>();
        EventActionUtil.forLeaderboardsOfEvent(context, eventId, new LeaderboardCallback() {
            @Override
            public void doForLeaderboard(LeaderboardContext leaderboardContext) {
                result.add(leaderboardContext.getRegattaWithProgress(context));
            }
        });     
        return new ResultWithTTL<>(5 * 1000, new SortedSetResult<>(result));
    }
}
