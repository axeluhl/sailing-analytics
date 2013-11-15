package com.sap.sailing.datamining.impl;

import java.util.Collection;

import com.sap.sailing.datamining.FilterReceiver;

public interface SingleThreadedFilter<DataType> extends Cloneable, Runnable {

    public void setReceiver(FilterReceiver<DataType> receiver);
    
    public void setDataToFilter(Collection<DataType> data);

    public boolean isDone();

}
