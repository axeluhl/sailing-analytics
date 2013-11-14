package com.sap.sailing.datamining;

import java.util.Collection;
import java.util.concurrent.Future;

public interface DataRetriever<DataType> extends Future<Collection<DataType>> {

    public DataRetriever<DataType> startRetrieval();
    
}
