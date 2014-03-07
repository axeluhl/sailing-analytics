package com.sap.sse.datamining.impl.components;

import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Map.Entry;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
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

    private Executor executor;
    private final ProcessResultReceiver resultReceiver;

    private final Object monitorObject = new Object();
    private boolean workIsDone = false;
    private boolean processorTimedOut = true;

    public ProcessorQuery(Executor executor, DataSourceType dataSource) {
        this.executor = executor;
        this.dataSource = dataSource;
        resultReceiver = new ProcessResultReceiver();
    }
    
    public void setFirstProcessor(Processor<DataSourceType> firstProcessor) {
        this.firstProcessor = firstProcessor;
    }

    @Override
    public QueryResult<AggregatedType> run() {
        try {
            return run(0, TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
            // This code shouldn't be reached, because the timeout is deactivated (by the value 0 as timeout)
            LOGGER.log(Level.WARNING, "Got a TimeoutException that should never happen: ", e);
        }
        
        return null;
    }
    
    @Override
    public QueryResult<AggregatedType> run(long timeout, TimeUnit unit) throws TimeoutException {
        try {
            return processQuery(unit.toMillis(timeout));
        } catch (InterruptedException e) {
            LOGGER.log(Level.WARNING, "The query processing got interrupted.", e);
        }
        
        return null;
    }

    private QueryResult<AggregatedType> processQuery(long timeoutInMillis) throws InterruptedException, TimeoutException {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    firstProcessor.onElement(dataSource);
                    firstProcessor.finish();
                } catch (InterruptedException e) {
                    LOGGER.log(Level.WARNING, "The query processing got interrupted.", e);
                }
            }
        });
        waitTillWorkIsDone(timeoutInMillis);
        return resultReceiver.getResult();
    }

    private void waitTillWorkIsDone(long timeoutInMillis) throws InterruptedException, TimeoutException {
        setUpTimeoutTimer(timeoutInMillis);
        synchronized (monitorObject) {
            while (!workIsDone) {
                monitorObject.wait();
                if (processorTimedOut && !workIsDone) {
                    firstProcessor.abort();
                    throw new TimeoutException("The query processing timed out");
                }
            }
            workIsDone = false;
            processorTimedOut = true;
        }
    }
    
    private void setUpTimeoutTimer(long timeoutInMillis) {
        if (timeoutInMillis > 0) {
            Timer timeoutTimer = new Timer();
            timeoutTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    synchronized (monitorObject) {
                        monitorObject.notify();
                    }
                }
            }, timeoutInMillis);
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
                processorTimedOut = false;
                monitorObject.notify();
            }
        }
        
        @Override
        public void abort() {
            result = null;
        }
        
        public QueryResult<AggregatedType> getResult() {
            return result;
        }
        
    }

}
