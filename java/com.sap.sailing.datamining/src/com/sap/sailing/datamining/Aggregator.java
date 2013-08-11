package com.sap.sailing.datamining;

import java.util.Collection;

public interface Aggregator<ExtractedType, AggregatedType> {

    public String getName();
    
    public AggregatedType aggregate(Collection<ExtractedType> data);

}
