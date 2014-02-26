package com.sap.sailing.datamining.function.impl;

import java.util.Collection;
import java.util.concurrent.ThreadPoolExecutor;

import com.sap.sailing.datamining.function.FunctionRetrievalWorker;

public class PartitioningParallelMarkedFunctionRetriever extends AbstractPartitioningParallelFunctionRetriever {

    public PartitioningParallelMarkedFunctionRetriever(Collection<Class<?>> classesToScan, ThreadPoolExecutor executor) {
        super(classesToScan, executor);
    }
    
    @Override
    protected FunctionRetrievalWorker createWorker(Iterable<Class<?>> classesToScan) {
        return FunctionRetrievalWorker.Util.createMarkedFunctionRetrievalWorker(classesToScan, this);
    }

}
