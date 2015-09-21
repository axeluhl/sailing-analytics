package com.sap.sailing.gwt.ui.shared.dispatch.event;

import java.util.UUID;

import com.google.gwt.core.shared.GwtIncompatible;
import com.sap.sailing.gwt.ui.shared.dispatch.Action;
import com.sap.sailing.gwt.ui.shared.dispatch.DispatchContext;
import com.sap.sailing.gwt.ui.shared.dispatch.ResultWithTTL;

public class GetRegattaStatisticsAction implements Action<ResultWithTTL<EventStatisticsDTO>>{
    
    private UUID eventId;
    private String regattaId;
    
    protected GetRegattaStatisticsAction() {
    }

    public GetRegattaStatisticsAction(UUID eventId, String regattaId) {
        this.eventId = eventId;
        this.regattaId = regattaId;
    }

    @Override
    @GwtIncompatible
    public ResultWithTTL<EventStatisticsDTO> execute(DispatchContext context) throws Exception {
        StatisticsCalculator statisticsCalculator = new StatisticsCalculator();
        statisticsCalculator.doForLeaderboard(EventActionUtil.getLeaderboardContext(context, eventId, regattaId));
        return statisticsCalculator.getResult();
    }

}
