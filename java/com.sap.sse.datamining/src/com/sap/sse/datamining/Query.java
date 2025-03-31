package com.sap.sse.datamining;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.sap.sse.datamining.data.QueryResult;

public interface Query<ResultType> {
    
    public enum QueryType { STATISTIC, DIMENSION_VALUES, OTHER }
    
    public QueryState getState();
    
    public Class<ResultType> getResultType();
    
    public AdditionalQueryData getAdditionalData();
    
    public <T extends AdditionalQueryData> T getAdditionalData(Class<T> additionalDataType);
    
    public QueryResult<ResultType> run();

    public QueryResult<ResultType> run(long timeout, TimeUnit unit) throws TimeoutException;
    
    public void abort();

}
