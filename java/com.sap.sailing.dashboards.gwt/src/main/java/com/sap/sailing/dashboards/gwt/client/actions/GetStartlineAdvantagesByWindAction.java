package com.sap.sailing.dashboards.gwt.client.actions;

/**
 * @author Alexander Ries (D062114)
 *
 */

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sailing.dashboards.gwt.client.RibDashboardServiceAsync;
import com.sap.sailing.dashboards.gwt.shared.dto.StartlineAdvantagesWithMaxAndAverageDTO;
import com.sap.sse.gwt.client.async.AsyncAction;

public class GetStartlineAdvantagesByWindAction implements AsyncAction<StartlineAdvantagesWithMaxAndAverageDTO> {

    private final RibDashboardServiceAsync ribDashboardService;
    private final String leaderboardName;

    public GetStartlineAdvantagesByWindAction(RibDashboardServiceAsync ribDashboardService, String leaderboardName) {
        this.ribDashboardService = ribDashboardService;
        this.leaderboardName = leaderboardName;
    }

    @Override
    public void execute(AsyncCallback<StartlineAdvantagesWithMaxAndAverageDTO> callback) {
        ribDashboardService.getAdvantagesOnStartline(leaderboardName, callback);
    }}