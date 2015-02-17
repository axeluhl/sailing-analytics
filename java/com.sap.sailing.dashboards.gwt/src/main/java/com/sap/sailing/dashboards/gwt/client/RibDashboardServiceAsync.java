package com.sap.sailing.dashboards.gwt.client;

import java.io.Serializable;
import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sailing.dashboards.gwt.shared.dto.RibDashboardRaceInfoDTO;
import com.sap.sailing.dashboards.gwt.shared.dto.startanalysis.StartAnalysisDTO;
import com.sap.sailing.domain.common.dto.CompetitorDTO;

public interface RibDashboardServiceAsync {

    void getLiveRaceInfo(String leaderboardName, AsyncCallback<RibDashboardRaceInfoDTO> callback);

    void getStartAnalysisListForCompetitorIDAndLeaderboardName(Serializable competitorID, String leaderboardName, AsyncCallback<List<StartAnalysisDTO>> callback);

    void getCompetitorsInRaceWithStateLive(AsyncCallback<List<CompetitorDTO>> callback);

}
