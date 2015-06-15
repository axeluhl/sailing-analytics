package com.sap.sailing.dashboards.gwt.client.actions;

/**
 * @author Alexander Ries (D062114)
 *
 */

import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sailing.dashboards.gwt.client.RibDashboardServiceAsync;
import com.sap.sse.common.Util.Pair;
import com.sap.sse.gwt.client.async.AsyncAction;

public class GetStartlineAdvantagesAction implements AsyncAction<List<Pair<Double, Double>>> {

    private final RibDashboardServiceAsync ribDashboardService;
    private final String leaderboardName;

    public GetStartlineAdvantagesAction(RibDashboardServiceAsync ribDashboardService, String leaderboardName) {
        this.ribDashboardService = ribDashboardService;
        this.leaderboardName = leaderboardName;
    }

    @Override
    public void execute(AsyncCallback<List<Pair<Double, Double>>> callback) {
        ribDashboardService.getAdvantagesOnStartline(leaderboardName, callback);
    }
}