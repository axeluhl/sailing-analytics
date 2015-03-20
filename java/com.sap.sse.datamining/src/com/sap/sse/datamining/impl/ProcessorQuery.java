package com.sap.sse.datamining.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sap.sse.datamining.AdditionalQueryData;
import com.sap.sse.datamining.AdditionalResultDataBuilder;
import com.sap.sse.datamining.Query;
import com.sap.sse.datamining.QueryState;
import com.sap.sse.datamining.components.Processor;
import com.sap.sse.datamining.impl.components.OverwritingResultDataBuilder;
import com.sap.sse.datamining.shared.AdditionalResultData;
import com.sap.sse.datamining.shared.GroupKey;
import com.sap.sse.datamining.shared.QueryResult;
import com.sap.sse.datamining.shared.QueryResultState;
import com.sap.sse.datamining.shared.impl.NullAdditionalResultData;
import com.sap.sse.datamining.shared.impl.QueryResultImpl;
import com.sap.sse.i18n.ResourceBundleStringMessages;

public abstract class ProcessorQuery<AggregatedType, DataSourceType> implements Query<AggregatedType> {
    
    private static final Logger LOGGER = Logger.getLogger(ProcessorQuery.class.getSimpleName());
    
    private final DataSourceType dataSource;
    private final Processor<DataSourceType, ?> firstProcessor;

    private final ProcessResultReceiver resultReceiver;
    
    private final ResourceBundleStringMessages stringMessages;
    private final Locale locale;
    private final AdditionalQueryData additionalData;

    private final Object monitorObject = new Object();
    private Thread workingThread;
    private QueryState state;
    
    /**
     * Creates a query
     * <ul>
     *   <li> with no {@link AdditionalQueryData} (more exactly with {@link NullAdditionalQueryData} as additional data).</li>
     *   <li> that returns a result without {@link AdditionalResultData} (more exactly with {@link NullAdditionalResultData} as additional data).</li>
     * </ul>
     * 
     * This is useful for non user specific queries, like retrieving the dimension values.
     */
    public ProcessorQuery(DataSourceType dataSource) {
        this(dataSource, null, null, AdditionalQueryData.NULL_INSTANCE);
    }

    /**
     * Creates a query that returns a result with additional data.
     */
    public ProcessorQuery(DataSourceType dataSource, ResourceBundleStringMessages stringMessages, Locale locale, AdditionalQueryData additionalData) {
        this.dataSource = dataSource;
        this.stringMessages = stringMessages;
        this.locale = locale;
        this.additionalData = additionalData;
        state = QueryState.NOT_STARTED;

        resultReceiver = new ProcessResultReceiver();
        firstProcessor = createFirstProcessor();
    }

    protected abstract Processor<DataSourceType, ?> createFirstProcessor();
    
    @Override
    public QueryState getState() {
        return state;
    }
    
    @Override
    public AdditionalQueryData getAdditionalData() {
        return additionalData;
    }

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
        state = QueryState.RUNNING;
        final long startTime = System.nanoTime();
        startWorking();
        waitTillWorkIsDone(timeoutInMillis);
        final long endTime = System.nanoTime();
        
        logOccuredFailuresAndThrowSevereFailure();

        long calculationTimeInNanos = endTime - startTime;
        Map<GroupKey, AggregatedType> results = resultReceiver.getResult();
        QueryResultState resultState = state.asResultState();
        
        if (stringMessages != null && locale != null) {
            AdditionalResultDataBuilder additionalDataBuilder = new OverwritingResultDataBuilder();
            additionalDataBuilder = firstProcessor.getAdditionalResultData(additionalDataBuilder);
            return new QueryResultImpl<>(resultState, results, additionalDataBuilder.build(calculationTimeInNanos, stringMessages, locale));
        } else {
            return new QueryResultImpl<>(resultState, results);
        }
    }

    private void startWorking() {
        workingThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    firstProcessor.processElement(dataSource);
                    firstProcessor.finish();
                } catch (InterruptedException e) {
                    if (state == QueryState.TIMED_OUT) {
                        LOGGER.log(Level.INFO, "The query processing timed out.");
                    } else if (state == QueryState.ABORTED) {
                        LOGGER.log(Level.INFO, "The query processing got aborted.");
                    } else if (state == QueryState.ERROR) {
                        LOGGER.log(Level.INFO, "A severe failure occured during the query processing.");
                    } else {
                        LOGGER.log(Level.WARNING, "The query processing got interrupted.", e);
                    }
                }
            }
        });
        workingThread.start();
    }

    private void waitTillWorkIsDone(long timeoutInMillis) throws InterruptedException, TimeoutException {
        setUpTimeoutTimer(timeoutInMillis);
        synchronized (monitorObject) {
            while (getState() == QueryState.RUNNING) {
                monitorObject.wait();
                if (processingHasToBeAborted()) {
                    firstProcessor.abort();
                    workingThread.interrupt();
                    if (state == QueryState.TIMED_OUT) {
                        throw new TimeoutException("The query processing timed out");
                    }
                    break;
                }
            }
        }
    }
    
    private boolean processingHasToBeAborted() {
        return state == QueryState.TIMED_OUT || state == QueryState.ABORTED || state == QueryState.ERROR;
    }

    private void setUpTimeoutTimer(long timeoutInMillis) {
        if (timeoutInMillis > 0) {
            Timer timeoutTimer = new Timer();
            timeoutTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    synchronized (monitorObject) {
                        state = QueryState.TIMED_OUT;
                        monitorObject.notify();
                    }
                }
            }, timeoutInMillis);
        }
    }

    private void logOccuredFailuresAndThrowSevereFailure() {
        for (Throwable failure : resultReceiver.getOccuredFailures()) {
            LOGGER.log(Level.SEVERE, "An error occured during the processing of an instruction: ", failure);
        }
        if (state == QueryState.ERROR) {
            throw new RuntimeException("A severe failure occured during the processing of an instruction", resultReceiver.getSevereFailure());
        }
    }
    
    @Override
    public void abort() {
        synchronized (monitorObject) {
            state = QueryState.ABORTED;
            monitorObject.notify();
        }
    }

    public Processor<Map<GroupKey, AggregatedType>, Void> getResultReceiver() {
        return resultReceiver;
    }
    
    private class ProcessResultReceiver implements Processor<Map<GroupKey, AggregatedType>, Void> {
        
        private final ReentrantLock resultsLock;
        private Map<GroupKey, AggregatedType> results;
        private List<Throwable> occuredFailures;
        private Throwable severeFailure;
        
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
            if (isSevereFailure(failure)) {
                severeFailure = failure;
                synchronized (monitorObject) {
                    state = QueryState.ERROR;
                    monitorObject.notify();
                }
            } else {
                state = QueryState.FAILURE;
                occuredFailures.add(failure);
            }
        }

        private boolean isSevereFailure(Throwable failure) {
            return !(failure instanceof Exception) || failure instanceof RejectedExecutionException;
        }

        @Override
        public void finish() throws InterruptedException {
            synchronized (monitorObject) {
                state = QueryState.NORMAL;
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
        
        public Throwable getSevereFailure() {
            return severeFailure;
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
