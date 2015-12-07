package com.sap.sailing.dashboards.gwt.client.actions;

/**
 * @author Alexander Ries (D062114)
 *
 */

import com.google.gwt.core.shared.GwtIncompatible;
import com.sap.sailing.dashboards.gwt.server.util.actions.startlineadvantage.StartlineAdvantagesByWindCalculator;
import com.sap.sailing.dashboards.gwt.shared.dispatch.DashboardDispatchContext;
import com.sap.sailing.dashboards.gwt.shared.dispatch.RequiresLiveRaceAndCachesMovingAverageAction;
import com.sap.sailing.dashboards.gwt.shared.dto.StartlineAdvantagesWithMaxAndAverageDTO;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.gwt.dispatch.client.exceptions.DispatchException;
import com.sap.sse.common.impl.MillisecondsTimePoint;

public class GetStartlineAdvantagesByWindAction  extends RequiresLiveRaceAndCachesMovingAverageAction<StartlineAdvantagesWithMaxAndAverageDTO> {

    public GetStartlineAdvantagesByWindAction() {}
    
    public GetStartlineAdvantagesByWindAction(String leaderboardName) {
        super(leaderboardName);
    }
    
    @Override
    @GwtIncompatible
    public StartlineAdvantagesWithMaxAndAverageDTO execute(DashboardDispatchContext dashboardDispatchContext) throws DispatchException {
        StartlineAdvantagesWithMaxAndAverageDTO result = new StartlineAdvantagesWithMaxAndAverageDTO();
        TrackedRace liveRace = super.getLiveRace(dashboardDispatchContext);
        if (liveRace != null) {
            StartlineAdvantagesByWindCalculator startlineAdvantagesByWindCalculator = new StartlineAdvantagesByWindCalculator(dashboardDispatchContext);
            result = startlineAdvantagesByWindCalculator.getStartLineAdvantagesAccrossLineFromTrackedRaceAtTimePoint(liveRace, MillisecondsTimePoint.now());
        }
        if(result != null && result.maximum != null) {
            super.addValueToMovingAverage(result.maximum, dashboardDispatchContext.getMovingAveragesCache());
            result.average = dashboardDispatchContext.getMovingAveragesCache().getValueForKey(getKeyForMovingAverage());
        }
        return result;
    }

    @Override
    protected String getKeyForMovingAverage() {
        return super.getLeaderboardName();
    }
}