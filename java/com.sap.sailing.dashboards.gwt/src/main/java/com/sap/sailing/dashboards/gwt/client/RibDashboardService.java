package com.sap.sailing.dashboards.gwt.client;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
import com.sap.sailing.dashboards.gwt.shared.dto.RibDashboardRaceInfoDTO;
import com.sap.sailing.domain.common.NoWindException;

@RemoteServiceRelativePath("ribdashboards")
public interface RibDashboardService extends RemoteService {

    RibDashboardRaceInfoDTO getLiveRaceInfo(String leaderboardName, String competitorName) throws NoWindException;
    
}
