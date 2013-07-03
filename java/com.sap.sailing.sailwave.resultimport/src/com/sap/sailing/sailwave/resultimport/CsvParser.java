package com.sap.sailing.sailwave.resultimport;

import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.resultimport.RegattaResults;

public interface CsvParser {
    RegattaResults parseResults() throws Exception;
    
    String getBoatClass();
    
    String getFilename();
    
    TimePoint getLastModified();
}
