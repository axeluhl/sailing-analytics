package com.sap.sse.datamining.impl.components.deprecated;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadPoolExecutor;

import com.sap.sse.datamining.components.ParallelGrouper;
import com.sap.sse.datamining.shared.GroupKey;
import com.sap.sse.datamining.workers.GroupingWorker;
import com.sap.sse.datamining.workers.WorkerBuilder;

public class PartitioningParallelGrouper<DataType> extends AbstractParallelComponent<Collection<DataType>, Map<GroupKey, Collection<DataType>>>
                                                   implements ParallelGrouper<DataType> {

    private WorkerBuilder<GroupingWorker<DataType>> workerBuilder;

    public PartitioningParallelGrouper(WorkerBuilder<GroupingWorker<DataType>> workerBuilder, ThreadPoolExecutor executor) {
        super(executor);
        this.workerBuilder = workerBuilder;
    }

    @Override
    protected void setUpWorkersFor(Collection<DataType> data) {
        List<DataType> dataAsList = new ArrayList<DataType>(data);
        final int workerAmount = (int) (getExecutor().getCorePoolSize() * 0.5);
        final int partitionSize = (int) Math.ceil((double) dataAsList.size() / workerAmount);
        for (int i = 0; i < dataAsList.size(); i += partitionSize) {
            List<DataType> partition = dataAsList.subList(i, i + Math.min(partitionSize, dataAsList.size() - i));
            
            GroupingWorker<DataType> worker = workerBuilder.build();
            worker.setReceiver(this);
            worker.setDataToGroup(partition);
            addWorker(worker);
        }
    }

    @Override
    protected Map<GroupKey, Collection<DataType>> finalizeData() {
        Map<GroupKey, Collection<DataType>> data = new HashMap<GroupKey, Collection<DataType>>();
        for (Map<GroupKey, Collection<DataType>> result : getResults()) {
            data.putAll(result);
        }
        return Collections.unmodifiableMap(new ConcurrentHashMap<GroupKey, Collection<DataType>>(data));
    }

}
