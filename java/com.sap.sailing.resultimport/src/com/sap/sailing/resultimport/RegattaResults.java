package com.sap.sailing.resultimport;

import java.util.List;
import java.util.Map;

public interface RegattaResults {
    /**
     * Data such as regatta name, operating institution, comments and dates
     */
    Map<String, String> getMetadata();
    
    List<CompetitorRow> getCompetitorResults();
}
