package com.sap.sailing.datamining;

import java.util.Collection;
import java.util.Map;

import com.sap.sailing.datamining.shared.GroupKey;

public interface AggregationWorker<ExtractedType, AggregatedType> extends ComponentWorker<Map<GroupKey, AggregatedType>> {
    
    public void setDataToAggregate(Map<GroupKey, Collection<ExtractedType>> data);

}
