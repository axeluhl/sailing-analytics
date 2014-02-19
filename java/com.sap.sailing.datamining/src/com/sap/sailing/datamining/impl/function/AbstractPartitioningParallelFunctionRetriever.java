package com.sap.sailing.datamining.impl.function;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ThreadPoolExecutor;

import com.sap.sse.datamining.functions.Function;
import com.sap.sse.datamining.functions.ParallelFunctionRetriever;
import com.sap.sse.datamining.impl.components.AbstractParallelComponent;
import com.sap.sse.datamining.workers.ComponentWorker;

public abstract class AbstractPartitioningParallelFunctionRetriever
                      extends AbstractParallelComponent<Void, Collection<Function<?>>>
                      implements ParallelFunctionRetriever {

    private List<Class<?>> classesToScan;

    public AbstractPartitioningParallelFunctionRetriever(Collection<Class<?>> classesToScan, ThreadPoolExecutor executor) {
        super(executor);
        this.classesToScan = new ArrayList<>(classesToScan);
    }

    @Override
    protected void setUpWorkersFor(Void data) {
        final int workerAmount = calculateMaximumAmountOfWorkers();
        if (workerAmount >= classesToScan.size()) {
            simpleWorkerSetUp();
        } else {
            final int partitionSize = calculatePartitionSize(workerAmount);
            partitioningWorkerSetUp(partitionSize);
        }
    }

    public int calculatePartitionSize(final int workerAmount) {
        return (int) Math.ceil((double) classesToScan.size() / workerAmount);
    }

    public int calculateMaximumAmountOfWorkers() {
        return (int) (getExecutor().getCorePoolSize() * 0.5);
    }

    private void simpleWorkerSetUp() {
        for (Class<?> classToScan : classesToScan) {
            addWorker(createWorker(classToScan));
        }
    }

    private ComponentWorker<Collection<Function<?>>> createWorker(Class<?> classToScan) {
        Collection<Class<?>> classToScanAsCollection = new HashSet<>();
        classToScanAsCollection.add(classToScan);
        return createWorker(classToScanAsCollection);
    }

    private void partitioningWorkerSetUp(int partitionSize) {
        for (int i = 0; i < classesToScan.size(); i += partitionSize) {
            List<Class<?>> partition = classesToScan.subList(i, i + Math.min(partitionSize, classesToScan.size() - i));
            addWorker(createWorker(partition));
        }
    }
    
    protected abstract FunctionRetrievalWorker createWorker(Iterable<Class<?>> classesToScan);

    @Override
    protected Collection<Function<?>> finalizeData() {
        Collection<Function<?>> data = new HashSet<>();
        for (Collection<Function<?>> results : getResults()) {
            data.addAll(results);
        }
        return Collections.unmodifiableCollection(new CopyOnWriteArrayList<Function<?>>(data));
    }

}
