package com.sap.sailing.datamining.impl;

import java.util.Collection;

import com.sap.sailing.datamining.FiltrationWorker;
import com.sap.sailing.datamining.WorkReceiver;

public abstract class AbstractSingleThreadedFilter<DataType> implements FiltrationWorker<DataType> {

    protected WorkReceiver<Collection<DataType>> receiver;
    protected Collection<DataType> data;
    private boolean isDone;

    @Override
    public void run() {
        receiver.receiveWork(filterData());
        isDone = true;
    }

    protected abstract Collection<DataType> filterData();

    @Override
    public void setReceiver(WorkReceiver<Collection<DataType>> receiver) {
        this.receiver = receiver;
    }

    @Override
    public void setDataToFilter(Collection<DataType> data) {
        this.data = data;
    }

    @Override
    public boolean isDone() {
        return isDone;
    }

}