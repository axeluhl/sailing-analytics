package com.sap.sailing.datamining.impl;

import java.util.ArrayList;
import java.util.Collection;

import com.sap.sailing.datamining.FiltrationWorker;

public abstract class AbstractFiltrationWorker<DataType> extends AbstractComponentWorker<Collection<DataType>>
                                                         implements FiltrationWorker<DataType> {

    private Collection<DataType> data;

    @Override
    protected Collection<DataType> doWork() {
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
    public void setDataToFilter(Collection<DataType> data) {
        this.data = data;
    }

}