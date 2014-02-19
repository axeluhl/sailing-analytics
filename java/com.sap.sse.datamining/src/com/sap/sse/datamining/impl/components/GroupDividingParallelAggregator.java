package com.sap.sse.datamining.impl.components;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadPoolExecutor;

import com.sap.sse.datamining.components.ParallelAggregator;
import com.sap.sse.datamining.shared.GroupKey;
import com.sap.sse.datamining.workers.AggregationWorker;
import com.sap.sse.datamining.workers.WorkerBuilder;

public class GroupDividingParallelAggregator<ExtractedType, AggregatedType> extends AbstractParallelComponent<Map<GroupKey, Collection<ExtractedType>>,
                                                                                                              Map<GroupKey, AggregatedType>>
                                                                            implements ParallelAggregator<ExtractedType, AggregatedType> {

    private String name;
    
    private WorkerBuilder<AggregationWorker<ExtractedType, AggregatedType>> workerBuilder;

    public GroupDividingParallelAggregator(String name, WorkerBuilder<AggregationWorker<ExtractedType, AggregatedType>> workerBuilder, ThreadPoolExecutor executor) {
        super(executor);
        this.name = name;
        this.workerBuilder = workerBuilder;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    protected void setUpWorkersFor(Map<GroupKey, Collection<ExtractedType>> data) {
        for (Entry<GroupKey, Collection<ExtractedType>> entry : data.entrySet()) {
            AggregationWorker<ExtractedType, AggregatedType> worker = workerBuilder.build();
            worker.setReceiver(this);
            
            Map<GroupKey, Collection<ExtractedType>> dataToExtractFrom = new HashMap<GroupKey, Collection<ExtractedType>>();
            dataToExtractFrom.put(entry.getKey(), entry.getValue());
            worker.setDataToAggregate(dataToExtractFrom);
            
            addWorker(worker);
        }
    }

    @Override
    protected Map<GroupKey, AggregatedType> finalizeData() {
        Map<GroupKey, AggregatedType> data = new HashMap<GroupKey, AggregatedType>();
        for (Map<GroupKey, AggregatedType> result : getResults()) {
            data.putAll(result);
        }
        return Collections.unmodifiableMap(new ConcurrentHashMap<GroupKey, AggregatedType>(data));
    }

}
