package com.sap.sailing.dashboards.gwt.client;

import java.util.List;

import com.google.gwt.user.client.rpc.RemoteService;
import com.sap.sailing.dashboards.gwt.shared.dto.RibDashboardRaceInfoDTO;
import com.sap.sailing.dashboards.gwt.shared.dto.StartlineAdvantagesWithMaxAndAverageDTO;
import com.sap.sailing.dashboards.gwt.shared.dto.startanalysis.StartAnalysisDTO;
import com.sap.sailing.domain.common.NoWindException;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.domain.common.dto.CompetitorDTO;

public interface RibDashboardService extends RemoteService {
    
    RegattaAndRaceIdentifier getIDFromRaceThatIsLive(String leaderboardName);
    
    RibDashboardRaceInfoDTO getLiveRaceInfo(String leaderboardName) throws NoWindException;
    
    RegattaAndRaceIdentifier getIDFromRaceThatTakesWindFixesNow(String leaderboardName);
    
    List<StartAnalysisDTO> getStartAnalysisListForCompetitorIDAndLeaderboardName(String competitorIdAsString, String leaderboardName);
    
    List<CompetitorDTO> getCompetitorsInLeaderboard(String leaderboardName);
    
    StartlineAdvantagesWithMaxAndAverageDTO getAdvantagesOnStartline(String leaderboardName);
}
