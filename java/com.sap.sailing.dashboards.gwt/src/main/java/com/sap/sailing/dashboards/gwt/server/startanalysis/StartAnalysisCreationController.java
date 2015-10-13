package com.sap.sailing.dashboards.gwt.server.startanalysis;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.sap.sailing.dashboards.gwt.shared.dto.startanalysis.StartAnalysisDTO;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.server.RacingEventService;

public class StartAnalysisCreationController extends AbstractStartAnalysisCreationValidator {

    private StartAnalysisDTOFactory startAnalysisDTOFactory;
    
    private static final Logger logger = Logger.getLogger(StartAnalysisCreationController.class.getName());
    
    public StartAnalysisCreationController(RacingEventService racingEventService) {
        startAnalysisDTOFactory = new StartAnalysisDTOFactory(racingEventService);
    }

    public StartAnalysisDTO checkStartAnalysisForCompetitorInTrackedRace(Competitor competitor, TrackedRace trackedRace) {
        StartAnalysisDTO result = null;
        if (competitor != null && trackedRace != null) {
            if (threeCompetitorsPassedSecondWayPoint(trackedRace) && raceProgressedFarEnough(competitor, trackedRace)) {
                logger.log(Level.INFO, "Creating startanalysis for race " + trackedRace.getRace().getName() + " and competitor: " + competitor.getName());
                result = startAnalysisDTOFactory.createStartAnalysisForCompetitorAndTrackedRace(competitor, trackedRace);

            } else {
                logger.log(Level.INFO, "Waiting to create startanalysis for race " + trackedRace.getRace().getName() + " and competitor: " + competitor.getName());
            }
        }
        return result;
    }
}
