package com.sap.sailing.datamining.impl;

import java.util.ArrayList;
import java.util.Collection;

import com.sap.sailing.datamining.FiltrationWorker;
import com.sap.sailing.datamining.WorkReceiver;

public abstract class AbstractFiltrationWorker<DataType> implements FiltrationWorker<DataType> {

    //TODO make private if possible
    private WorkReceiver<Collection<DataType>> receiver;
    private Collection<DataType> data;
    private boolean isDone;

    @Override
    public void run() {
        receiver.receiveWork(filterData());
        isDone = true;
    }

    private Collection<DataType> filterData() {
        Collection<DataType> filteredData = new ArrayList<DataType>();
        for (DataType dataEntry : data) {
            if (matches(dataEntry)) {
                filteredData.add(dataEntry);
            }
        }
        return filteredData;
    }

    protected abstract boolean matches(DataType dataEntry);

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