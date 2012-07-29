package com.sap.sailing.winregatta.resultimport;

import java.util.List;

public interface RegattaResults {
    /**
     * Data such as regatta name, operating institution, comments and dates
     */
    List<String> getMetadata();
    
    List<CompetitorResult> getCompetitorResults();
}
