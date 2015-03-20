package com.sap.sse.datamining;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.sap.sse.datamining.shared.QueryResult;

public interface Query<AggregatedType> {
    
    public enum QueryType { STATISTIC, DIMENSION_VALUES, OTHER }
    
    public QueryState getState();
    
    public AdditionalQueryData getAdditionalData();
    
    public QueryResult<AggregatedType> run();

    public QueryResult<AggregatedType> run(long timeout, TimeUnit unit) throws TimeoutException;
    
    public void abort();

}
