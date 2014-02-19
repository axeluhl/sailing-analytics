package com.sap.sailing.datamining.impl.aggregators;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.sap.sse.datamining.impl.workers.AbstractComponentWorker;
import com.sap.sse.datamining.shared.GroupKey;
import com.sap.sse.datamining.workers.AggregationWorker;

public abstract class AbstractAggregationWorker<ExtractedType, AggregatedType> extends AbstractComponentWorker<Map<GroupKey, AggregatedType>>
                                                                               implements AggregationWorker<ExtractedType, AggregatedType> {

    private Map<GroupKey, Collection<ExtractedType>> data;
    
    @Override
    protected Map<GroupKey, AggregatedType> doWork() {
        Map<GroupKey, AggregatedType> aggregatedData = new HashMap<GroupKey, AggregatedType>();
        for (Entry<GroupKey, Collection<ExtractedType>> dataEntry : getData().entrySet()) {
            AggregatedType aggregation = aggregate(dataEntry.getValue());
            aggregatedData.put(dataEntry.getKey(), aggregation);
        }
        return aggregatedData;
    }

    protected abstract AggregatedType aggregate(Collection<ExtractedType> value);

    @Override
    public void setDataToAggregate(Map<GroupKey, Collection<ExtractedType>> data) {
        this.data = data;
    }
    
    public Map<GroupKey, Collection<ExtractedType>> getData() {
        return data;
    }
    
}
