package com.sap.sailing.datamining.impl.function;

import java.util.Collection;
import java.util.concurrent.ThreadPoolExecutor;

public class PartitionParallelExternalFunctionRetriever extends AbstractPartitioningParallelFunctionRetriever {

    public PartitionParallelExternalFunctionRetriever(Collection<Class<?>> externalClasses, ThreadPoolExecutor executor) {
        super(externalClasses, executor);
    }

    @Override
    protected FunctionRetrievalWorker createWorker(Iterable<Class<?>> classesToScan) {
        return FunctionRetrievalWorker.Util.createExternalFunctionRetrievalWorker(classesToScan, this);
    }

}
