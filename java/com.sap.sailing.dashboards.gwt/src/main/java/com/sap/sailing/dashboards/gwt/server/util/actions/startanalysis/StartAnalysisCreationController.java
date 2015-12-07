package com.sap.sailing.dashboards.gwt.server.util.actions.startanalysis;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gwt.core.shared.GwtIncompatible;
import com.sap.sailing.dashboards.gwt.shared.dispatch.DashboardDispatchContext;
import com.sap.sailing.dashboards.gwt.shared.dto.startanalysis.StartAnalysisDTO;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.tracking.TrackedRace;

@GwtIncompatible
public final class StartAnalysisCreationController extends AbstractStartAnalysisCreationValidator {

    private static final Logger logger = Logger.getLogger(StartAnalysisCreationController.class.getName());
    
    private StartAnalysisCreationController(DashboardDispatchContext ctx) {
        
    }

    public static StartAnalysisDTO checkStartAnalysisForCompetitorInTrackedRace(DashboardDispatchContext dashboardDispatchContext, Competitor competitor, TrackedRace trackedRace) {
        StartAnalysisDTO result = null;
        if (competitor != null && trackedRace != null) {
            if (threeCompetitorsPassedSecondWayPoint(trackedRace) && raceProgressedFarEnough(competitor, trackedRace)) {
                logger.log(Level.INFO, "Creating startanalysis for race " + trackedRace.getRace().getName() + " and competitor: " + competitor.getName());
                result = StartAnalysisDTOFactory.createStartAnalysisForCompetitorAndTrackedRace(dashboardDispatchContext, competitor, trackedRace);
            } else {
                logger.log(Level.INFO, "Waiting to create startanalysis for race " + trackedRace.getRace().getName() + " and competitor: " + competitor.getName());
            }
        }
        return result;
    }
}
