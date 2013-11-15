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
import com.sap.sailing.datamining.FilterReceiver;
import com.sap.sailing.datamining.WorkerBuilder;

public class ParallelFilter<DataType> implements Filter<DataType>, FilterReceiver<DataType> {

    private ThreadPoolExecutor executor;
    private final WorkerBuilder<SingleThreadedFilter<DataType>> workerBuilder;
    private HashSet<SingleThreadedFilter<DataType>> workers;
    private Collection<DataType> data;

    public ParallelFilter(WorkerBuilder<SingleThreadedFilter<DataType>> workerBuilder, ThreadPoolExecutor executor) {
        this.workerBuilder = workerBuilder;
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
        final int partitionSize = (int) Math.ceil((double) dataAsList.size() / workerAmount);
        for (int i = 0; i < dataAsList.size(); i += partitionSize) {
            List<DataType> partition = dataAsList.subList(i, i + Math.min(partitionSize, dataAsList.size() - i));
            
            SingleThreadedFilter<DataType> worker = workerBuilder.build();
            worker.setReceiver(this);
            worker.setDataToFilter(partition);
            workers.add(worker);
        }
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
    public void addFilteredData(Collection<DataType> data) {
        this.data.addAll(data);
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
