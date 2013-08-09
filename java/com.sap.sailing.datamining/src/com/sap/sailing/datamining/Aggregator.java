package com.sap.sailing.datamining;

import java.util.Collection;

public interface Aggregator<ExtractedType, AggregatedType> {
    
    public AggregatedType aggregate(Collection<ExtractedType> data);

}
