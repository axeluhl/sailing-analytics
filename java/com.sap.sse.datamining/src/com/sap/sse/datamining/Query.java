package com.sap.sse.datamining;

import java.util.concurrent.ExecutionException;

import com.sap.sse.datamining.shared.QueryResult;

public interface Query<AggregatedType> {
    
    public QueryResult<AggregatedType> run() throws InterruptedException, ExecutionException;

}
