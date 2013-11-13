package com.sap.sailing.datamining.impl;

import java.util.Collection;
import java.util.concurrent.Executor;

import com.sap.sailing.datamining.DataRetriever;
import com.sap.sailing.server.RacingEventService;

public class ParallelDataRetriever<DataType> implements DataRetriever<DataType> {

    /**
     * Creates a new parallel working data retriever with the given executer and data retriever as base for the workers.
     * If a new worker is needed, the worker base will be cloned.
     */
    public ParallelDataRetriever(AbstractSingleThreadedDataRetriever<DataType> workerBase, Executor executor) {
        // TODO Auto-generated constructor stub
    }

    @Override
    public Collection<DataType> retrieveData(RacingEventService racingEventService) {
        // TODO Auto-generated method stub
        return null;
    }

}
