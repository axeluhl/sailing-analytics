package com.sap.sailing.datamining;

import java.util.Collection;

public interface Aggregator {
    
    public double aggregate(Collection<GPSFixWithContext> data);

}
