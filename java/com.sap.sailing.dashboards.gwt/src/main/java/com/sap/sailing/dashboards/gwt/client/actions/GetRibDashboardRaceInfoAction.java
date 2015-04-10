package com.sap.sailing.dashboards.gwt.client.actions;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sailing.dashboards.gwt.client.RibDashboardServiceAsync;
import com.sap.sailing.dashboards.gwt.shared.dto.RibDashboardRaceInfoDTO;
import com.sap.sse.gwt.client.async.AsyncAction;

public class GetRibDashboardRaceInfoAction implements AsyncAction<RibDashboardRaceInfoDTO> {

    private final RibDashboardServiceAsync ribDashboardService;
    private final String leaderboardName;

    public GetRibDashboardRaceInfoAction(RibDashboardServiceAsync ribDashboardService, String leaderboardName) {
        this.ribDashboardService = ribDashboardService;
        this.leaderboardName = leaderboardName;
    }

    @Override
    public void execute(AsyncCallback<RibDashboardRaceInfoDTO> callback) {
        ribDashboardService.getLiveRaceInfo(leaderboardName, callback);
    }
}