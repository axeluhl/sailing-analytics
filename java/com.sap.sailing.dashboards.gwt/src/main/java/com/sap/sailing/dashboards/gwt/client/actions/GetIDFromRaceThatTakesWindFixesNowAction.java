package com.sap.sailing.dashboards.gwt.client.actions;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sailing.dashboards.gwt.client.RibDashboardServiceAsync;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sse.gwt.client.async.AsyncAction;

/**
 * @author Alexander Ries (D062114)
 *
 */
public class GetIDFromRaceThatTakesWindFixesNowAction implements AsyncAction<RegattaAndRaceIdentifier> {

    private final RibDashboardServiceAsync ribDashboardService;
    private final String leaderboardName;

    public GetIDFromRaceThatTakesWindFixesNowAction(RibDashboardServiceAsync ribDashboardService, String leaderboardName) {
        this.ribDashboardService = ribDashboardService;
        this.leaderboardName = leaderboardName;
    }

    @Override
    public void execute(AsyncCallback<RegattaAndRaceIdentifier> callback) {
        ribDashboardService.getIDFromRaceThatTakesWindFixesNow(leaderboardName, callback);
    }
}