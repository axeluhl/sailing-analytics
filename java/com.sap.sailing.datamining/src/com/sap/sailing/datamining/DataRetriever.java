package com.sap.sailing.datamining;

import java.util.List;

public interface DataRetriever<T> {
    
    public T getTarget();
    
    /**
     * Retrieves all GPS-Fixes from its target.
     */
    public List<GPSFixWithContext> retrieveData();

}
