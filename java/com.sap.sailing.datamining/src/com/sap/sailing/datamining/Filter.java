package com.sap.sailing.datamining;

import java.util.Collection;
import java.util.concurrent.Future;

public interface Filter<DataType> extends Future<Collection<DataType>> {
    
    public Filter<DataType> startFiltering(Collection<DataType> data);

}