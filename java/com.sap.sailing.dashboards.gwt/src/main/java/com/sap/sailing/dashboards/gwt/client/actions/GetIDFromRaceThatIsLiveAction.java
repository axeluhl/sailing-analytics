package com.sap.sailing.dashboards.gwt.client.actions;

import com.google.gwt.core.shared.GwtIncompatible;
import com.sap.sailing.dashboards.gwt.shared.dispatch.DashboardDispatchContext;
import com.sap.sailing.dashboards.gwt.shared.dispatch.RequiresLiveRaceAction;
import com.sap.sailing.dashboards.gwt.shared.dto.RaceIdDTO;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sse.gwt.dispatch.shared.exceptions.DispatchException;

/**
 * @author Alexander Ries (D062114)
 *
 */
public class GetIDFromRaceThatIsLiveAction extends RequiresLiveRaceAction<RaceIdDTO> {

    public GetIDFromRaceThatIsLiveAction() {}
    
    public GetIDFromRaceThatIsLiveAction(String leaderboardName) {
        super(leaderboardName);
    }

    @Override
    @GwtIncompatible
    public RaceIdDTO execute(DashboardDispatchContext dashboardDispatchContext) throws DispatchException {
        RaceIdDTO result = new RaceIdDTO();
        TrackedRace liveRace = super.getLiveRace(dashboardDispatchContext);
        if (liveRace != null) {
            result.setRaceId(liveRace.getRaceIdentifier());
        }
        return result;
    }
}
