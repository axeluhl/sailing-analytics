package com.sap.sailing.datamining;

import java.util.List;

public interface DataRetriever<T> {
    
    public T getTarget();
    
    /**
     * Retrieves all GPS-Fixes from its target.
     * @param initialContext Used to build the contexts of the retrieved GPS-Fixes.
     */
    public List<GPSFixWithContext> retrieveData(GPSFixContext initialContext);

}
