package com.sap.sailing.gwt.ui.shared.dispatch.event;

import java.util.UUID;

import com.google.gwt.core.shared.GwtIncompatible;
import com.sap.sailing.gwt.ui.shared.dispatch.DispatchContext;
import com.sap.sailing.gwt.ui.shared.dispatch.ResultWithTTL;
import com.sap.sailing.gwt.ui.shared.dispatch.event.EventActionUtil.RaceCallback;

@GwtIncompatible
public class RaceListDataCalculator implements RaceCallback {
    private final RaceRefreshCalculator refreshCalculator = new RaceRefreshCalculator();
//    private final RegattaProgressCalculator progressCalculator = new RegattaProgressCalculator();
    private final RaceListViewDTO result = new RaceListViewDTO();

    @Override
    public void doForRace(RaceContext context) {
        refreshCalculator.doForRace(context);
//        progressCalculator.doForRace(context);
        result.add(context.getLiveRaceOrNull());
        result.add(context.getFinishedRaceOrNull());
    }
    
    public ResultWithTTL<RaceListViewDTO> getResult(DispatchContext context, UUID eventId, String regattaId) {
        LeaderboardContext leaderboardContext = EventActionUtil.getLeaderboardContext(context, eventId, regattaId);
        result.setProgress(leaderboardContext.getRegattaWithProgress(context));
        return new ResultWithTTL<>(EventActionUtil.getEventStateDependentTTL(context, eventId, refreshCalculator.getTTL()), result);
    }

}
