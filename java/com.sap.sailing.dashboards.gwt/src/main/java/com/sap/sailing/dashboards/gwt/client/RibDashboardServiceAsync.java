package com.sap.sailing.dashboards.gwt.client;

import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sailing.dashboards.gwt.shared.dto.RibDashboardRaceInfoDTO;
import com.sap.sailing.dashboards.gwt.shared.dto.startanalysis.StartAnalysisDTO;
import com.sap.sailing.domain.common.dto.CompetitorDTO;

public interface RibDashboardServiceAsync {

    void getLiveRaceInfo(String leaderboardName, AsyncCallback<RibDashboardRaceInfoDTO> callback);

    void getStartAnalysisListForCompetitorIDAndLeaderboardName(String competitorIdAsString, String leaderboardName, AsyncCallback<List<StartAnalysisDTO>> callback);

    void getCompetitorsInLeaderboard(String leaderboardName, AsyncCallback<List<CompetitorDTO>> callback);

}
