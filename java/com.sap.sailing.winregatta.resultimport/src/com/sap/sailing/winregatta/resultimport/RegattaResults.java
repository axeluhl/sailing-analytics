package com.sap.sailing.winregatta.resultimport;

import java.util.List;
import java.util.Map;

public interface RegattaResults {
    /**
     * Data such as regatta name, operating institution, comments and dates
     */
    Map<String, String> getMetadata();
    
    List<CompetitorResult> getCompetitorResults();
}
