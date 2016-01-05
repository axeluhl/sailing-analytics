package com.sap.sailing.dashboards.gwt.client.actions;

import com.google.gwt.core.shared.GwtIncompatible;
import com.sap.sailing.dashboards.gwt.shared.dispatch.DashboardAction;
import com.sap.sailing.dashboards.gwt.shared.dispatch.DashboardDispatchContext;
import com.sap.sailing.dashboards.gwt.shared.dto.LeaderboardCompetitorsDTO;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.gwt.dispatch.client.exceptions.DispatchException;
import com.sap.sse.common.impl.MillisecondsTimePoint;

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
        result.setCompetitors(dashboardDispatchContext.getRacingEventService().getBaseDomainFactory().getCompetitorDTOList(lb.getCompetitorsFromBestToWorst(MillisecondsTimePoint.now())));
        return result;
    }
}
