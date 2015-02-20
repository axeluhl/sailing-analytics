package com.sap.sailing.dashboards.gwt.server.startanalysis;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.sap.sailing.dashboards.gwt.shared.dto.startanalysis.StartAnalysisDTO;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.server.RacingEventService;

public class StartAnalysisCreationController extends AbstractStartAnalysisCreationValidator {

    private StartAnalysisDTOFactory startAnalysisDTOFactory;
    private StartAnalysisCache startAnalysisCache;
    
    private static final Logger logger = Logger.getLogger(StartAnalysisCache.class.getName());
    
    public StartAnalysisCreationController(RacingEventService racingEventService) {
        startAnalysisDTOFactory = new StartAnalysisDTOFactory(racingEventService);
        startAnalysisCache = new StartAnalysisCache();
    }

    public void checkForNewStartAnalysisForCompetitorInTrackedRace(Competitor competitor, TrackedRace trackedRace) {
        if (startAnalysisCache.containsStartAnalysisForCompetitorAndTrackedRace(competitor, trackedRace) == false &&
            threeCompetitorsPassedSecondWayPoint(trackedRace) &&
            competitorPassedSecondWayPoint(competitor, trackedRace)) 
        {
            logger.log(Level.INFO, "Trigger StartAnalysisDTO creation");
            if(competitor == null){
                logger.log(Level.INFO, "COMPETITOR NULL");
            }
            StartAnalysisDTO startAnalysisDTO = startAnalysisDTOFactory.createStartAnalysisForCompetitorAndTrackedRace(competitor,  trackedRace);
            startAnalysisCache.addStartAnalysisDTOFor(startAnalysisDTO, competitor, trackedRace);
        }
    }

    public StartAnalysisCache getStartAnalysisCache() {
        return startAnalysisCache;
    }
}
