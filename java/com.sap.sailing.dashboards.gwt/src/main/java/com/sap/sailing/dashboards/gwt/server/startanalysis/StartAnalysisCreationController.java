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
        StartAnalysisDTO startAnalysisDTO;
        if (threeCompetitorsPassedSecondWayPoint(trackedRace) &&
            raceProgressedFarEnough(competitor, trackedRace)) 
        {
            logger.log(Level.INFO, "Trigger StartAnalysisDTO creation");
            startAnalysisDTO = startAnalysisDTOFactory.createStartAnalysisForCompetitorAndTrackedRace(competitor,  trackedRace);
            return startAnalysisDTO;
        }else{
            return null;
        }
    }
}
