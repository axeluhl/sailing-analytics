package com.sap.sse.datamining;

import java.util.concurrent.ExecutionException;

import com.sap.sse.datamining.shared.QueryResult;

public interface Query<DataType, AggregatedType> {
    
    public QueryResult<AggregatedType> run() throws InterruptedException, ExecutionException;

}
