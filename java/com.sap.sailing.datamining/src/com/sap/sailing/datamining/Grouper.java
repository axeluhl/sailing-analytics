package com.sap.sailing.datamining;

import java.util.Collection;
import java.util.Map;

public interface Grouper {

    public Map<String, Collection<GPSFixWithContext>> group(Collection<GPSFixWithContext> data);
    
}
