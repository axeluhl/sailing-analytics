package com.sap.sse.datamining.workers;

import java.util.Collection;
import java.util.Map;

import com.sap.sse.datamining.shared.GroupKey;

public interface AggregationWorker<ExtractedType, AggregatedType> extends ComponentWorker<Map<GroupKey, AggregatedType>> {
    
    public void setDataToAggregate(Map<GroupKey, Collection<ExtractedType>> data);

}
