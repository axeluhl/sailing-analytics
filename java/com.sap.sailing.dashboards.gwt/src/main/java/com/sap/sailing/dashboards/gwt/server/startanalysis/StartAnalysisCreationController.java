package com.sap.sailing.dashboards.gwt.server.startanalysis;

import com.sap.sailing.dashboards.gwt.shared.dto.startanalysis.StartAnalysisDTO;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.server.RacingEventService;

public class StartAnalysisCreationController extends AbstractStartAnalysisCreationValidator {

    private StartAnalysisDTOFactory startAnalysisDTOFactory;
    private StartAnalysisCache startAnalysisCache;

    public StartAnalysisCreationController(RacingEventService racingEventService) {
        startAnalysisDTOFactory = new StartAnalysisDTOFactory(racingEventService);
        startAnalysisCache = new StartAnalysisCache();
    }

    public void checkForNewStartAnalysisForCompetitorInTrackedRace(Competitor competitor, TrackedRace trackedRace) {
        if (startAnalysisCache.containsStartAnalysisForCompetitorAndTrackedRace(competitor, trackedRace) == false &&
            threeCompetitorsPassedSecondWayPoint(trackedRace) &&
            competitorPassedSecondWayPoint(competitor, trackedRace)) 
        {
            StartAnalysisDTO startAnalysisDTO = startAnalysisDTOFactory.createStartAnalysisForCompetitorAndTrackedRace(competitor,  trackedRace);
            startAnalysisCache.addStartAnalysisDTOFor(startAnalysisDTO, competitor, trackedRace);
        }
    }

    public StartAnalysisCache getStartAnalysisCache() {
        return startAnalysisCache;
    }
}
