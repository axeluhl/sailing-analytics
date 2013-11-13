package com.sap.sailing.datamining.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.sap.sailing.datamining.Filter;

public class ParallelFilter<DataType> implements Filter<DataType> {
    
    private ThreadPoolExecutor executor;
    private SingleThreadedFilter<DataType> workerBase;
    private HashSet<SingleThreadedFilter<DataType>> workers;
    private Collection<DataType> data;

    public ParallelFilter(SingleThreadedFilter<DataType> workerBase, ThreadPoolExecutor executor) {
        this.workerBase = workerBase;
        this.executor = executor;
        workers = new HashSet<SingleThreadedFilter<DataType>>();
        data = new ArrayList<DataType>();
    }

    @Override
    public Filter<DataType> startFiltering(Collection<DataType> data) {
        setUpWorkersFor(data);
        for (SingleThreadedFilter<DataType> worker : workers) {
            executor.execute(worker);
        }
        return this;
    }

    private void setUpWorkersFor(Collection<DataType> data) {
        List<DataType> dataAsList = new ArrayList<DataType>(data);
        final int workerAmount = (int) (executor.getCorePoolSize() * 0.5);
        //TODO Split list into equal chunks and create a worker for each
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return false;
    }

    @Override
    public boolean isCancelled() {
        return false;
    }

    @Override
    public boolean isDone() {
        for (SingleThreadedFilter<DataType> worker : workers) {
            if (!worker.isDone()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public Collection<DataType> get() throws InterruptedException, ExecutionException {
        while (!isDone()) {
            Thread.sleep(100);
        }
        return getDataAsUnmodifiableConcurrentCollection();
    }

    @Override
    public Collection<DataType> get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException,
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
        return getDataAsUnmodifiableConcurrentCollection();
    }

    private Collection<DataType> getDataAsUnmodifiableConcurrentCollection() {
        return Collections.unmodifiableCollection(new CopyOnWriteArrayList<DataType>(data));
    }

}
