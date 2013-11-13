package com.sap.sailing.datamining.data.impl;

import java.util.Collection;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.sap.sailing.datamining.Filter;

public class NoFilter<DataType> implements Filter<DataType> {

    private boolean isDone;
    private Collection<DataType> data;

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
        return isDone;
    }

    @Override
    public Collection<DataType> get() throws InterruptedException, ExecutionException {
        while (!isDone()) {
            Thread.sleep(100);
        }
        return data;
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
        return data;
    }

    @Override
    public Filter<DataType> startFiltering(Collection<DataType> data) {
        this.data = data;
        isDone = true;
        return this;
    }

}
