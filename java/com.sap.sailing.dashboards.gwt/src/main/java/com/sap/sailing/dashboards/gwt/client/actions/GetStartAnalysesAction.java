package com.sap.sailing.dashboards.gwt.client.actions;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.core.shared.GwtIncompatible;
import com.sap.sailing.dashboards.gwt.server.util.actions.startanalysis.StartAnalysisCreationController;
import com.sap.sailing.dashboards.gwt.shared.dispatch.DashboardAction;
import com.sap.sailing.dashboards.gwt.shared.dispatch.DashboardDispatchContext;
import com.sap.sailing.dashboards.gwt.shared.dto.StartAnalysesDTO;
import com.sap.sailing.dashboards.gwt.shared.dto.startanalysis.StartAnalysisDTO;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.gwt.dispatch.client.exceptions.DispatchException;

/**
 * @author Alexander Ries (D062114)
 *
 */
public class GetStartAnalysesAction implements DashboardAction<StartAnalysesDTO> {

    private String leaderboarName;
    private String competitorIdAsString;

    @SuppressWarnings("unused")
    private GetStartAnalysesAction() {
    }

    public GetStartAnalysesAction(String leaderboardName, String competitorIdAsString) {
        this.leaderboarName = leaderboardName;
        this.competitorIdAsString = competitorIdAsString;
    }

    @Override
    @GwtIncompatible
    public StartAnalysesDTO execute(DashboardDispatchContext dashboardDispatchContext) throws DispatchException {
        StartAnalysesDTO result = new StartAnalysesDTO();
        List<StartAnalysisDTO> startanalyses = new ArrayList<StartAnalysisDTO>();
        if (competitorIdAsString != null && leaderboarName != null) {
            try {
                Competitor competitor = dashboardDispatchContext.getRacingEventService().getBaseDomainFactory().getCompetitorStore().getExistingCompetitorByIdAsString(
                        competitorIdAsString);
                Leaderboard leaderboard = dashboardDispatchContext.getRacingEventService().getLeaderboardByName(this.leaderboarName);
                if (leaderboard != null) {
                    for (RaceColumn column : leaderboard.getRaceColumns()) {
                        for (Fleet fleet : column.getFleets()) {
                            TrackedRace race = column.getTrackedRace(fleet);
                            if (race != null) {
                                StartAnalysisDTO startAnalysisDTO = StartAnalysisCreationController.checkStartAnalysisForCompetitorInTrackedRace(dashboardDispatchContext, competitor, race);
                                if (startAnalysisDTO != null) {
                                    startanalyses.add(startAnalysisDTO);
                                }
                            }
                        }
                    }

                }
            } catch (NullPointerException e) {
                // logger.log(Level.INFO, "", e);
            }
        }
        result.setStartAnalyses(startanalyses);
        return result;
    }
}
