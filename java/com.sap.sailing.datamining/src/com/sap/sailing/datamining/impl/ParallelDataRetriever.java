package com.sap.sailing.datamining.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.sap.sailing.datamining.DataReceiver;
import com.sap.sailing.datamining.DataRetriever;
import com.sap.sailing.datamining.SingleThreadedDataRetriever;
import com.sap.sailing.domain.leaderboard.LeaderboardGroup;
import com.sap.sailing.server.RacingEventService;

public class ParallelDataRetriever<DataType> implements DataRetriever<DataType>, DataReceiver<DataType> {

    private Executor executor;
    private Set<SingleThreadedDataRetriever<DataType>> workers;
    private Collection<DataType> data;

    /**
     * Creates a new parallel working data retriever with the given executer and data retriever as base for the workers.
     * If a new worker is needed, the worker base will be cloned.
     */
    public ParallelDataRetriever(SingleThreadedDataRetriever<DataType> workerBase, Executor executor,
            RacingEventService racingService) {
        this.executor = executor;
        workers = new HashSet<SingleThreadedDataRetriever<DataType>>();
        data = new ArrayList<DataType>();

        for (LeaderboardGroup group : racingService.getLeaderboardGroups().values()) {
            SingleThreadedDataRetriever<DataType> worker = workerBase.clone();
            worker.setReceiver(this);
            worker.setGroup(group);
            workers.add(worker);
        }
    }

    @Override
    public DataRetriever<DataType> startRetrieval() {
        for (SingleThreadedDataRetriever<DataType> worker : workers) {
            executor.execute(worker);
        }
        return this;
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

    @Override
    public boolean isDone() {
        for (SingleThreadedDataRetriever<DataType> worker : workers) {
            if (!worker.isDone()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public synchronized void addData(Collection<DataType> data) {
        this.data.addAll(data);
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
