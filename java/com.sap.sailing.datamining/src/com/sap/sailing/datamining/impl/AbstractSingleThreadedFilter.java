package com.sap.sailing.datamining.impl;

import java.util.Collection;

import com.sap.sailing.datamining.FilterReceiver;
import com.sap.sailing.datamining.SingleThreadedFilter;

public abstract class AbstractSingleThreadedFilter<DataType> implements SingleThreadedFilter<DataType> {

    protected FilterReceiver<DataType> receiver;
    protected Collection<DataType> data;
    private boolean isDone;

    @Override
    public void run() {
        receiver.addFilteredData(filterData());
        isDone = true;
    }

    protected abstract Collection<DataType> filterData();

    @Override
    public void setReceiver(FilterReceiver<DataType> receiver) {
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