package com.sap.sse.datamining.impl.components;

import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sap.sse.datamining.Query;
import com.sap.sse.datamining.components.Processor;
import com.sap.sse.datamining.shared.GroupKey;
import com.sap.sse.datamining.shared.QueryResult;
import com.sap.sse.datamining.shared.Unit;
import com.sap.sse.datamining.shared.impl.QueryResultImpl;

public class ProcessorQuery<AggregatedType, DataSourceType> implements Query<AggregatedType> {
    
    private static final Logger LOGGER = Logger.getLogger(ProcessorQuery.class.getSimpleName());
    
    private final DataSourceType dataSource;
    private Processor<DataSourceType> firstProcessor;
    
    private final ProcessResultReceiver resultReceiver;

    private final Object monitorObject = new Object();
    private boolean workIsDone;

    public ProcessorQuery(DataSourceType dataSource) {
        this.dataSource = dataSource;
        resultReceiver = new ProcessResultReceiver();
    }
    
    public void setFirstProcessor(Processor<DataSourceType> firstProcessor) {
        this.firstProcessor = firstProcessor;
    }

    @Override
    public QueryResult<AggregatedType> run() {
        try {
            return processQuery();
        } catch (InterruptedException e) {
            LOGGER.log(Level.WARNING, "The query processing got interrupted.", e);
        }
        
        return null;
    }

    private QueryResult<AggregatedType> processQuery() throws InterruptedException {
        firstProcessor.onElement(dataSource);
        firstProcessor.finish();
        waitTillWorkIsDone();
        return resultReceiver.getResult();
    }
    
    private void waitTillWorkIsDone() throws InterruptedException {
        synchronized (monitorObject) {
            while (!workIsDone) {
                monitorObject.wait();
            }
            workIsDone = false;
        }
    }

    Processor<Map<GroupKey, AggregatedType>> getResultReceiver() {
        return resultReceiver;
    }
    
    private class ProcessResultReceiver implements Processor<Map<GroupKey, AggregatedType>> {
        
        private QueryResult<AggregatedType> result;

        @Override
        public void onElement(Map<GroupKey, AggregatedType> groupedAggregations) {
            result = constructResult(groupedAggregations);
        }

        private QueryResult<AggregatedType> constructResult(Map<GroupKey, AggregatedType> groupedAggregations) {
            QueryResultImpl<AggregatedType> result = new QueryResultImpl<>(0, 0, "", Unit.None, 0);
            for (Entry<GroupKey, AggregatedType> groupedAggregationsEntry : groupedAggregations.entrySet()) {
                result.addResult(groupedAggregationsEntry.getKey(), groupedAggregationsEntry.getValue());
            }
            return result;
        }

        @Override
        public void finish() throws InterruptedException {
            synchronized (monitorObject) {
                workIsDone = true;
                monitorObject.notify();
            }
        }
        
        public QueryResult<AggregatedType> getResult() {
            return result;
        }
        
    }

}
