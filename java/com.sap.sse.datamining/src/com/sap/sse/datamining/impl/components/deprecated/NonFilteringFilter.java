package com.sap.sse.datamining.impl.components.deprecated;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.sap.sse.datamining.components.ParallelFilter;

public class NonFilteringFilter<DataType> implements ParallelFilter<DataType> {

    private Collection<DataType> data;
    
    public NonFilteringFilter() {
        data = new ArrayList<DataType>();
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
        return true;
    }

    @Override
    public Collection<DataType> get() {
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
    public ParallelFilter<DataType> start(Collection<DataType> data) {
        this.data = data;
        return this;
    }

}
