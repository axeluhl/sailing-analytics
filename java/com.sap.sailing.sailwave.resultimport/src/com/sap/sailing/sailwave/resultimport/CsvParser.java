package com.sap.sailing.sailwave.resultimport;

import com.sap.sailing.resultimport.RegattaResults;
import com.sap.sse.common.TimePoint;

public interface CsvParser {
    RegattaResults parseResults() throws Exception;
    
    String getBoatClass();
    
    String getFilename();
    
    TimePoint getLastModified();
}
