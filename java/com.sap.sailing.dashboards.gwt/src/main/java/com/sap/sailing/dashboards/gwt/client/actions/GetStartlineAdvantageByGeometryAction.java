package com.sap.sailing.dashboards.gwt.client.actions;

import com.google.gwt.core.shared.GwtIncompatible;
import com.sap.sailing.dashboards.gwt.client.startanalysis.StartlineAdvantageType;
import com.sap.sailing.dashboards.gwt.server.util.actions.startlineadvantage.StartlineAdvantageByGeometryCalculator;
import com.sap.sailing.dashboards.gwt.shared.dispatch.DashboardDispatchContext;
import com.sap.sailing.dashboards.gwt.shared.dispatch.RequiresLiveRaceAndCachesMovingAverageAction;
import com.sap.sailing.dashboards.gwt.shared.dto.StartLineAdvantageDTO;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.gwt.dispatch.client.exceptions.DispatchException;

/**
 * @author Alexander Ries (D062114)
 *
 */
public class GetStartlineAdvantageByGeometryAction extends RequiresLiveRaceAndCachesMovingAverageAction<StartLineAdvantageDTO> {

    public GetStartlineAdvantageByGeometryAction() {}

    public GetStartlineAdvantageByGeometryAction(String leaderboardName) {
        super(leaderboardName);
    }

    @Override
    @GwtIncompatible
    public StartLineAdvantageDTO execute(DashboardDispatchContext dashboardDispatchContext) throws DispatchException {
        StartLineAdvantageDTO result = new StartLineAdvantageDTO();
        Double startlineAdvantageByGeometry = null;
        TrackedRace liveRace = super.getLiveRace(dashboardDispatchContext);
        if (liveRace != null) {
            startlineAdvantageByGeometry = StartlineAdvantageByGeometryCalculator.calculateStartlineAdvantageByGeometry(liveRace);
        }
        result.startLineAdvatageType = StartlineAdvantageType.GEOMETRIC;
        result.startLineAdvantage = startlineAdvantageByGeometry;
        Double average = null;
        if (startlineAdvantageByGeometry != null) {
            super.addValueToMovingAverage(startlineAdvantageByGeometry.doubleValue(), dashboardDispatchContext.getMovingAveragesCache());
            average = dashboardDispatchContext.getMovingAveragesCache().getValueForKey(getKeyForMovingAverage());
        }
        result.average = average;
        return result;
    }

    @Override
    protected String getKeyForMovingAverage() {
        return super.getLeaderboardName();
    }
}
