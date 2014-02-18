package com.sap.sailing.datamining.impl.function;

import java.util.Collection;
import java.util.concurrent.ThreadPoolExecutor;

public class PartitioningParallelMarkedFunctionRetriever extends AbstractPartitioningParallelFunctionRetriever {

    public PartitioningParallelMarkedFunctionRetriever(Collection<Class<?>> classesToScan, ThreadPoolExecutor executor) {
        super(classesToScan, executor);
    }
    
    @Override
    protected FunctionRetrievalWorker createWorker(Iterable<Class<?>> classesToScan) {
        return FunctionRetrievalWorker.Util.createMarkedFunctionRetrievalWorker(classesToScan, this);
    }

}
