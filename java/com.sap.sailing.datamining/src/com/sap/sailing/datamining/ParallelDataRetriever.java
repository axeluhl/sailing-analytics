package com.sap.sailing.datamining;

import java.util.Collection;

import com.sap.sailing.datamining.impl.ParallelComponent;

public interface ParallelDataRetriever<DataType> extends ParallelComponent<Void, Collection<DataType>> {
    
}
