package com.sap.sailing.dashboards.gwt.client.actions;

import java.util.List;

import com.google.gwt.core.shared.GwtIncompatible;
import com.sap.sailing.dashboards.gwt.shared.dispatch.DashboardAction;
import com.sap.sailing.dashboards.gwt.shared.dispatch.DashboardDispatchContext;
import com.sap.sailing.dashboards.gwt.shared.dto.LeaderboardCompetitorsDTO;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.common.dto.CompetitorDTO;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sse.common.impl.MillisecondsTimePoint;
import com.sap.sse.gwt.dispatch.shared.exceptions.DispatchException;

/**
 * @author Alexander Ries (D062114)
 *
 */
public class GetCompetitorInLeaderboardAction implements DashboardAction<LeaderboardCompetitorsDTO> {

    private String leaderboarName;

    @SuppressWarnings("unused")
    private GetCompetitorInLeaderboardAction() {
    }

    public GetCompetitorInLeaderboardAction(String leaderboardName) {
        this.leaderboarName = leaderboardName;
    }

    @Override
    @GwtIncompatible
    public LeaderboardCompetitorsDTO execute(DashboardDispatchContext dashboardDispatchContext) throws DispatchException {
        LeaderboardCompetitorsDTO result = new LeaderboardCompetitorsDTO();
        Leaderboard lb = dashboardDispatchContext.getRacingEventService().getLeaderboardByName(this.leaderboarName);
        List<Competitor> competitorsFromBestToWorst = lb.getCompetitorsFromBestToWorst(MillisecondsTimePoint.now());
        List<CompetitorDTO> competitorDTOs = dashboardDispatchContext.getRacingEventService().getBaseDomainFactory().getCompetitorDTOList(competitorsFromBestToWorst);
        result.setCompetitors(competitorDTOs);
        return result;
    }
}
