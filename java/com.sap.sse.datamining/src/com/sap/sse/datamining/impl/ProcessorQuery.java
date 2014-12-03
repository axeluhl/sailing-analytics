package com.sap.sse.datamining.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sap.sse.datamining.AdditionalResultDataBuilder;
import com.sap.sse.datamining.Query;
import com.sap.sse.datamining.components.Processor;
import com.sap.sse.datamining.i18n.DataMiningStringMessages;
import com.sap.sse.datamining.impl.components.OverwritingResultDataBuilder;
import com.sap.sse.datamining.shared.GroupKey;
import com.sap.sse.datamining.shared.QueryResult;
import com.sap.sse.datamining.shared.impl.QueryResultImpl;

public abstract class ProcessorQuery<AggregatedType, DataSourceType> implements Query<AggregatedType> {
    
    private static final Logger LOGGER = Logger.getLogger(ProcessorQuery.class.getSimpleName());
    
    private final DataSourceType dataSource;
    private final Processor<DataSourceType, ?> firstProcessor;

    private final Executor executor;
    private final ProcessResultReceiver resultReceiver;
    
    private final DataMiningStringMessages stringMessages;
    private final Locale locale;

    private final Object monitorObject = new Object();
    private boolean workIsDone = false;
    private boolean processorTimedOut = false;
    
    /**
     * Creates a query that returns a result without any additional data (like the calculation time or the retrieved data amount).<br>
     * This is useful for non user specific queries, like retrieving the dimension values.
     */
    public ProcessorQuery(ThreadPoolExecutor executor, DataSourceType dataSource) {
        this(executor, dataSource, null, null);
    }

    /**
     * Creates a query that returns a result with additional data.
     */
    public ProcessorQuery(Executor executor, DataSourceType dataSource, DataMiningStringMessages stringMessages, Locale locale) {
        this.executor = executor;
        this.dataSource = dataSource;
        this.stringMessages = stringMessages;
        this.locale = locale;

        resultReceiver = new ProcessResultReceiver();
        firstProcessor = createFirstProcessor();
    }

    protected abstract Processor<DataSourceType, ?> createFirstProcessor();

    @Override
    public QueryResult<AggregatedType> run() {
        try {
            return run(0, TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
            // This code shouldn't be reached, because the timeout is deactivated (by the value 0 as timeout)
            LOGGER.log(Level.SEVERE, "Got a TimeoutException that should never happen: ", e);
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
                    firstProcessor.processElement(dataSource);
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
        
        logOccuredFailures();

        long calculationTimeInNanos = endTime - startTime;
        AdditionalResultDataBuilder additionalDataBuilder = new OverwritingResultDataBuilder();
        additionalDataBuilder = firstProcessor.getAdditionalResultData(additionalDataBuilder);
        Map<GroupKey, AggregatedType> results = resultReceiver.getResult();
        
        if (stringMessages != null && locale != null) {
            return new QueryResultImpl<>(results, additionalDataBuilder.build(calculationTimeInNanos, stringMessages, locale));
        } else {
            return new QueryResultImpl<>(results);
        }
    }

    private void logOccuredFailures() {
        for (Throwable failure : resultReceiver.getOccuredFailures()) {
            LOGGER.log(Level.SEVERE, "An error occured during the processing of an instruction: ", failure);
        }
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

    public Processor<Map<GroupKey, AggregatedType>, Void> getResultReceiver() {
        return resultReceiver;
    }
    
    private class ProcessResultReceiver implements Processor<Map<GroupKey, AggregatedType>, Void> {
        
        private final ReentrantLock resultsLock;
        private Map<GroupKey, AggregatedType> results;
        private List<Throwable> occuredFailures;
        
        public ProcessResultReceiver() {
            resultsLock = new ReentrantLock();
            results = new HashMap<>();
            occuredFailures = new ArrayList<>();
        }

        @Override
        public void processElement(Map<GroupKey, AggregatedType> groupedAggregations) {
            resultsLock.lock();
            try {
                results.putAll(groupedAggregations);
            } finally {
                resultsLock.unlock();
            }
        }
        
        @Override
        public void onFailure(Throwable failure) {
            occuredFailures.add(failure);
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
            results = new HashMap<>();
            occuredFailures = new ArrayList<>();
        }
        
        public Map<GroupKey, AggregatedType> getResult() {
            return results;
        }
        
        public List<Throwable> getOccuredFailures() {
            return occuredFailures;
        }

        @Override
        public AdditionalResultDataBuilder getAdditionalResultData(AdditionalResultDataBuilder additionalDataBuilder) {
            return additionalDataBuilder;
        }

        @SuppressWarnings("unchecked")
        @Override
        public Class<Map<GroupKey, AggregatedType>> getInputType() {
            return (Class<Map<GroupKey, AggregatedType>>)(Class<?>) Map.class;
        }

        @Override
        public Class<Void> getResultType() {
            return Void.class;
        }
        
    }

}
