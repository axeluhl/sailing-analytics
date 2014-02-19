package com.sap.sailing.datamining.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadPoolExecutor;

import com.sap.sse.datamining.components.ParallelExtractor;
import com.sap.sse.datamining.impl.components.AbstractParallelComponent;
import com.sap.sse.datamining.shared.GroupKey;
import com.sap.sse.datamining.shared.Unit;
import com.sap.sse.datamining.workers.ExtractionWorker;
import com.sap.sse.datamining.workers.WorkerBuilder;

public class GroupDividingParallelExtractor<DataType, ExtractedType> extends AbstractParallelComponent<Map<GroupKey, Collection<DataType>>,
                                                                                                       Map<GroupKey, Collection<ExtractedType>>>
                                                                     implements ParallelExtractor<DataType, ExtractedType> {

    private String signifier;
    private Unit unit;
    private int valueDecimals;
    
    private WorkerBuilder<ExtractionWorker<DataType, ExtractedType>> workerBuilder;

    public GroupDividingParallelExtractor(String signifier, Unit unit, int valueDecimals,
                                          WorkerBuilder<ExtractionWorker<DataType, ExtractedType>> workerBuilder, ThreadPoolExecutor executor) {
        super(executor);
        this.signifier = signifier;
        this.unit = unit;
        this.valueDecimals = valueDecimals;
        this.workerBuilder = workerBuilder;
    }

    @Override
    protected void setUpWorkersFor(Map<GroupKey, Collection<DataType>> data) {
        for (Entry<GroupKey, Collection<DataType>> entry : data.entrySet()) {
            ExtractionWorker<DataType, ExtractedType> worker = workerBuilder.build();
            worker.setReceiver(this);
            
            Map<GroupKey, Collection<DataType>> dataToExtractFrom = new HashMap<GroupKey, Collection<DataType>>();
            dataToExtractFrom.put(entry.getKey(), entry.getValue());
            worker.setDataToExtractFrom(dataToExtractFrom);
            
            addWorker(worker);
        }
    }

    @Override
    protected Map<GroupKey, Collection<ExtractedType>> finalizeData() {
        Map<GroupKey, Collection<ExtractedType>> data = new HashMap<GroupKey, Collection<ExtractedType>>();
        for (Map<GroupKey, Collection<ExtractedType>> result : getResults()) {
            data.putAll(result);
        }
        return Collections.unmodifiableMap(new ConcurrentHashMap<GroupKey, Collection<ExtractedType>>(data));
    }

    @Override
    public String getSignifier() {
        return signifier;
    }

    @Override
    public Unit getUnit() {
        return unit;
    }

    @Override
    public int getValueDecimals() {
        return valueDecimals;
    }

}
