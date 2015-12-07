package com.sap.sailing.dashboards.gwt.client.actions;

import com.google.gwt.core.shared.GwtIncompatible;
import com.sap.sailing.dashboards.gwt.shared.dispatch.DashboardAction;
import com.sap.sailing.dashboards.gwt.shared.dispatch.DashboardDispatchContext;
import com.sap.sailing.dashboards.gwt.shared.dto.RaceIdDTO;
import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.gwt.dispatch.client.exceptions.DispatchException;
import com.sap.sse.common.impl.MillisecondsTimePoint;

/**
 * @author Alexander Ries (D062114)
 *
 */
public class GetIDFromRaceThatTakesWindFixesNowAction implements DashboardAction<RaceIdDTO> {

    private String leaderboardName;

    @SuppressWarnings("unused")
    private GetIDFromRaceThatTakesWindFixesNowAction() {
    }
    
    public GetIDFromRaceThatTakesWindFixesNowAction(String leaderboardName) {
        this.leaderboardName = leaderboardName;
    }

    @Override
    @GwtIncompatible
    public RaceIdDTO execute(DashboardDispatchContext dashboardDispatchContext) throws DispatchException {
        RaceIdDTO result = new RaceIdDTO();
        Leaderboard leaderboard = dashboardDispatchContext.getRacingEventService().getLeaderboardByName(this.leaderboardName);
        if (leaderboard != null) {
            for (RaceColumn column : leaderboard.getRaceColumns()) {
                for (Fleet fleet : column.getFleets()) {
                    TrackedRace race = column.getTrackedRace(fleet);
                    if (race != null && race.takesWindFixWithTimePoint(MillisecondsTimePoint.now())) {
                        result.setRaceId(race.getRaceIdentifier());
                    }
                }
            }
        }
        return result;
    }
}