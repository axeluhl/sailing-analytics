package com.sap.sailing.datamining;

import java.util.concurrent.ExecutionException;

import com.sap.sailing.datamining.shared.QueryResult;

public interface Query<DataType, AggregatedType> {
    
    public QueryResult<AggregatedType> run() throws InterruptedException, ExecutionException;

}
