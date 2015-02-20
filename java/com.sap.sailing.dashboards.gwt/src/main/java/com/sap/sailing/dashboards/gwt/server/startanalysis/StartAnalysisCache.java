package com.sap.sailing.dashboards.gwt.server.startanalysis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sap.sailing.dashboards.gwt.shared.dto.startanalysis.StartAnalysisDTO;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.tracking.TrackedRace;

public class StartAnalysisCache {

    private Map<TrackedRace, Map<Competitor, StartAnalysisDTO>> startAnalyisisDTOForTrackedRacesAndCompetitors;
    
    public StartAnalysisCache(){
        startAnalyisisDTOForTrackedRacesAndCompetitors = new HashMap<TrackedRace, Map<Competitor,StartAnalysisDTO>>();
    }
    public void addStartAnalysisDTOFor(StartAnalysisDTO startAnalysisDTO, Competitor competitor, TrackedRace trackedRace) {
        Map<Competitor, StartAnalysisDTO> startAnalysisListForCompetitor = startAnalyisisDTOForTrackedRacesAndCompetitors
                .get(trackedRace);
        if (startAnalysisListForCompetitor == null) {
            startAnalysisListForCompetitor = new HashMap<Competitor, StartAnalysisDTO>();
            startAnalyisisDTOForTrackedRacesAndCompetitors.put(trackedRace, startAnalysisListForCompetitor);
        }
        if (startAnalysisListForCompetitor.get(competitor) == null) {
            startAnalysisListForCompetitor.put(competitor, startAnalysisDTO);
        } else {
            startAnalysisListForCompetitor.remove(competitor);
        }
    }

    public List<StartAnalysisDTO> getStartAnalysisDTOsForCompetitor(Competitor competitor) {
        List<StartAnalysisDTO> startAnalysisList = new ArrayList<StartAnalysisDTO>();
        for (Map<Competitor, StartAnalysisDTO> competitorStartAnalysisMap : startAnalyisisDTOForTrackedRacesAndCompetitors
                .values()) {
            StartAnalysisDTO startAnalysisDTO = competitorStartAnalysisMap.get(competitor);
            if (startAnalysisDTO != null)
                startAnalysisList.add(startAnalysisDTO);
        }
        java.util.Collections.sort(startAnalysisList);
        return startAnalysisList;
    }

    public boolean containsStartAnalysisForCompetitorAndTrackedRace(Competitor competitor, TrackedRace trackedRace) {
        if (startAnalyisisDTOForTrackedRacesAndCompetitors.containsKey(trackedRace)) {
            Map<Competitor, StartAnalysisDTO> competitorStartAnalysisMap = startAnalyisisDTOForTrackedRacesAndCompetitors
                    .get(trackedRace);
            if (competitorStartAnalysisMap.containsKey(competitor))
                return true;
        }
        return false;
    }
}
