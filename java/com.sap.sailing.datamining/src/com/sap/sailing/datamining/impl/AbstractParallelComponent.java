package com.sap.sailing.datamining.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.sap.sailing.datamining.ComponentWorker;
import com.sap.sailing.datamining.WorkReceiver;

public abstract class AbstractParallelComponent<WorkingDataType, ResultDataType> implements ParallelComponent<WorkingDataType, ResultDataType>,
                                                                                            WorkReceiver<ResultDataType> {

    private ThreadPoolExecutor executor;
    private Collection<ComponentWorker<ResultDataType>> workers;
    private Set<ResultDataType> results;
    
    public AbstractParallelComponent(ThreadPoolExecutor executor) {
        this.executor = executor;
        workers = new HashSet<ComponentWorker<ResultDataType>>();
        results = Collections.newSetFromMap(new ConcurrentHashMap<ResultDataType, Boolean>());
    }

    @Override
    public ParallelComponent<WorkingDataType, ResultDataType> start(WorkingDataType data) {
        setUpWorkersFor(data);
        for (ComponentWorker<ResultDataType> worker : workers) {
            executor.execute(worker);
        }
        return this;
    }

    protected abstract void setUpWorkersFor(WorkingDataType data);
    
    @Override
    public void receiveWork(ResultDataType result) {
        results.add(result);
    }
    
    protected Collection<ResultDataType> getResults() {
        return results;
    }

    @Override
    public ResultDataType get() throws InterruptedException, ExecutionException {
        while (!isDone()) {
            Thread.sleep(100);
        }
        return finalizeData();
    }

    @Override
    public ResultDataType get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException,
            TimeoutException {
        long timeRun = 0;
        long timeoutInMillis = unit.toMillis(timeout);
        while (!isDone() && timeRun < timeoutInMillis) {
            Thread.sleep(100);
            timeRun = timeRun + 100;
        }
        if (timeRun >= timeoutInMillis) {
            throw new TimeoutException();
        }
        return finalizeData();
    }

    protected abstract ResultDataType finalizeData();

    @Override
    public boolean isDone() {
        for (ComponentWorker<ResultDataType> worker : workers) {
            if (!worker.isDone()) {
                return false;
            }
        }
        return true;
    }

    protected ThreadPoolExecutor getExecutor() {
        return executor;
    }

    protected void addWorker(ComponentWorker<ResultDataType> worker) {
        workers.add(worker);
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return false;
    }

    @Override
    public boolean isCancelled() {
        return false;
    }

}
