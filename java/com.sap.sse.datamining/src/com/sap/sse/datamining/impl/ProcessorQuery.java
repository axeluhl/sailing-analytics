package com.sap.sse.datamining.impl;

import java.util.Locale;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sap.sse.datamining.AdditionalResultDataBuilder;
import com.sap.sse.datamining.Query;
import com.sap.sse.datamining.components.Processor;
import com.sap.sse.datamining.i18n.DataMiningStringMessages;
import com.sap.sse.datamining.impl.components.SumBuildingAndOverwritingResultDataBuilder;
import com.sap.sse.datamining.shared.GroupKey;
import com.sap.sse.datamining.shared.QueryResult;
import com.sap.sse.datamining.shared.impl.QueryResultImpl;

public class ProcessorQuery<AggregatedType, DataSourceType> implements Query<AggregatedType> {
    
    private static final Logger LOGGER = Logger.getLogger(ProcessorQuery.class.getSimpleName());
    
    private final DataSourceType dataSource;
    private Processor<DataSourceType> firstProcessor;

    private Executor executor;
    private final ProcessResultReceiver resultReceiver;
    
    private final DataMiningStringMessages stringMessages;
    private final Locale locale;

    private final Object monitorObject = new Object();
    private boolean workIsDone = false;
    private boolean processorTimedOut = false;

    public ProcessorQuery(Executor executor, DataSourceType dataSource, DataMiningStringMessages stringMessages, Locale locale) {
        this.executor = executor;
        this.dataSource = dataSource;
        this.stringMessages = stringMessages;
        this.locale = locale;
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
        processorTimedOut = false;
        final long startTime = System.nanoTime();
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    firstProcessor.onElement(dataSource);
                    firstProcessor.finish();
                } catch (InterruptedException e) {
                    if (processorTimedOut) {
                        LOGGER.log(Level.INFO, "The query processing timed out.");
                    } else {
                        LOGGER.log(Level.WARNING, "The query processing got interrupted.", e);
                    }
                }
            }
        });
        waitTillWorkIsDone(timeoutInMillis);
        final long endTime = System.nanoTime();

        long calculationTimeInNanos = endTime - startTime;
        AdditionalResultDataBuilder additionalDataBuilder = new SumBuildingAndOverwritingResultDataBuilder();
        additionalDataBuilder = firstProcessor.getAdditionalResultData(additionalDataBuilder);
        Map<GroupKey, AggregatedType> results = resultReceiver.getResult();
        return new QueryResultImpl<>(results, additionalDataBuilder.build(calculationTimeInNanos, stringMessages, locale));
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
        }
    }
    
    private void setUpTimeoutTimer(long timeoutInMillis) {
        if (timeoutInMillis > 0) {
            Timer timeoutTimer = new Timer();
            timeoutTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    synchronized (monitorObject) {
                        processorTimedOut = true;
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
        
        private Map<GroupKey, AggregatedType> results;

        @Override
        public void onElement(Map<GroupKey, AggregatedType> groupedAggregations) {
            results = groupedAggregations;
        }

        @Override
        public void finish() throws InterruptedException {
            synchronized (monitorObject) {
                workIsDone = true;
                monitorObject.notify();
            }
        }
        
        @Override
        public void abort() {
            results = null;
        }
        
        public Map<GroupKey, AggregatedType> getResult() {
            return results;
        }

        @Override
        public AdditionalResultDataBuilder getAdditionalResultData(AdditionalResultDataBuilder additionalDataBuilder) {
            return additionalDataBuilder;
        }
        
    }

}
