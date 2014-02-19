package com.sap.sse.datamining.impl.components;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.sap.sse.datamining.components.ParallelComponent;
import com.sap.sse.datamining.workers.ComponentWorker;
import com.sap.sse.datamining.workers.WorkReceiver;

public abstract class AbstractParallelComponent<WorkingType, ResultType> implements ParallelComponent<WorkingType, ResultType>,
                                                                                    WorkReceiver<ResultType> {

    private ThreadPoolExecutor executor;
    private Collection<ComponentWorker<ResultType>> workers;
    private Set<ResultType> results;
    
    public AbstractParallelComponent(ThreadPoolExecutor executor) {
        this.executor = executor;
        workers = new HashSet<ComponentWorker<ResultType>>();
        results = Collections.newSetFromMap(new ConcurrentHashMap<ResultType, Boolean>());
    }

    @Override
    public ParallelComponent<WorkingType, ResultType> start(WorkingType data) {
        setUpWorkersFor(data);
        for (ComponentWorker<ResultType> worker : workers) {
            executor.execute(worker);
        }
        return this;
    }

    protected abstract void setUpWorkersFor(WorkingType data);
    
    @Override
    public void receiveWork(ResultType result) {
        results.add(result);
    }
    
    protected Collection<ResultType> getResults() {
        return results;
    }

    @Override
    public ResultType get() throws InterruptedException, ExecutionException {
        while (!isDone()) {
            Thread.sleep(100);
        }
        return finalizeData();
    }

    @Override
    public ResultType get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException,
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

    protected abstract ResultType finalizeData();

    @Override
    public boolean isDone() {
        for (ComponentWorker<ResultType> worker : workers) {
            if (!worker.isDone()) {
                return false;
            }
        }
        return true;
    }

    protected ThreadPoolExecutor getExecutor() {
        return executor;
    }

    protected void addWorker(ComponentWorker<ResultType> worker) {
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
