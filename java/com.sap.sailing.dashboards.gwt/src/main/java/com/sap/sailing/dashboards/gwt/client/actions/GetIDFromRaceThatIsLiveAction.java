package com.sap.sailing.dashboards.gwt.client.actions;

import com.google.gwt.core.shared.GwtIncompatible;
import com.sap.sailing.dashboards.gwt.shared.RaceIdDTO;
import com.sap.sailing.dashboards.gwt.shared.dispatch.DashboardAction;
import com.sap.sailing.dashboards.gwt.shared.dispatch.DashboardDispatchContext;
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
public class GetIDFromRaceThatIsLiveAction implements DashboardAction<RaceIdDTO> {

    private String leaderboarName;

    @SuppressWarnings("unused")
    private GetIDFromRaceThatIsLiveAction() {
    }

    public GetIDFromRaceThatIsLiveAction(String leaderboardName) {
        this.leaderboarName = leaderboardName;
    }

    @Override
    @GwtIncompatible
    public RaceIdDTO execute(DashboardDispatchContext ctx) throws DispatchException {
        RaceIdDTO result = new RaceIdDTO();
        TrackedRace liveRace = null;
        Leaderboard leaderboard = ctx.getRacingEventService().getLeaderboardByName(this.leaderboarName);
        if (leaderboard != null) {
            for (RaceColumn column : leaderboard.getRaceColumns()) {
                for (Fleet fleet : column.getFleets()) {
                    TrackedRace race = column.getTrackedRace(fleet);
                    if (race != null && race.isLive(MillisecondsTimePoint.now())) {
                        liveRace = race;
                    }
                }
            }
        }
        if (liveRace != null)
            result.setRaceId(liveRace.getRaceIdentifier());
        return result;
    }
}
