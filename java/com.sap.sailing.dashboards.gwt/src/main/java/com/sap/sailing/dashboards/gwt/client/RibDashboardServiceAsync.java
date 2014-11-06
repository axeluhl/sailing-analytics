package com.sap.sailing.dashboards.gwt.client;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sailing.dashboards.gwt.shared.dto.RibDashboardRaceInfoDTO;

public interface RibDashboardServiceAsync {

    void getLiveRaceInfo(String leaderboardGroupName, String competitorName, AsyncCallback<RibDashboardRaceInfoDTO> callback);

}
