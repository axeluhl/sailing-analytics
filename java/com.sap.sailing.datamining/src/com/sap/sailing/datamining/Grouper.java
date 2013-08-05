package com.sap.sailing.datamining;

import java.util.Collection;
import java.util.Map;

public interface Grouper<K> {

    public Map<K, Collection<GPSFixWithContext>> group(Collection<GPSFixWithContext> data);
    
}
