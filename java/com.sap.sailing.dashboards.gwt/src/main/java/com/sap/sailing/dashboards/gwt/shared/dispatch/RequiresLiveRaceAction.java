package com.sap.sailing.dashboards.gwt.shared.dispatch;

import com.google.gwt.core.shared.GwtIncompatible;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.gwt.dispatch.client.Result;

/**
 * @author Alexander Ries (D062114)
 *
 */
public abstract class RequiresLiveRaceAction<R extends Result> implements DashboardAction<R>{
    
    private String leaderboardName;
    
    public RequiresLiveRaceAction() {}
    
    public RequiresLiveRaceAction(String leaderboardName) {
        this.leaderboardName = leaderboardName;
    }

    @GwtIncompatible
    protected TrackedRace getLiveRace(DashboardDispatchContext dashboardDispatchContext) {
        dashboardDispatchContext.getDashboardLiveRaceProvider().validateLiveRaceForLeaderboardName(this.leaderboardName);
        return dashboardDispatchContext.getDashboardLiveRaceProvider().getLiveRaceForLeaderboardName(this.leaderboardName);
    }
    
    protected String getLeaderboardName() {
        return this.leaderboardName;
    }
}
