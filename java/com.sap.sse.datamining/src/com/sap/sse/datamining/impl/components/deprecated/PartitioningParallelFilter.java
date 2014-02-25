package com.sap.sse.datamining.impl.components.deprecated;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ThreadPoolExecutor;

import com.sap.sse.datamining.components.ParallelFilter;
import com.sap.sse.datamining.workers.FiltrationWorker;
import com.sap.sse.datamining.workers.WorkerBuilder;

public class PartitioningParallelFilter<DataType> extends AbstractParallelComponent<Collection<DataType>, Collection<DataType>>
                                                  implements ParallelFilter<DataType> {

    private final WorkerBuilder<FiltrationWorker<DataType>> workerBuilder;

    public PartitioningParallelFilter(WorkerBuilder<FiltrationWorker<DataType>> workerBuilder, ThreadPoolExecutor executor) {
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
            
            FiltrationWorker<DataType> worker = workerBuilder.build();
            worker.setReceiver(this);
            worker.setDataToFilter(partition);
            addWorker(worker);
        }
    }

    @Override
    protected Collection<DataType> finalizeData() {
        Collection<DataType> data = new ArrayList<DataType>();
        for (Collection<DataType> results : getResults()) {
            data.addAll(results);
        }
        return Collections.unmodifiableCollection(new CopyOnWriteArrayList<DataType>(data));
    }

}
