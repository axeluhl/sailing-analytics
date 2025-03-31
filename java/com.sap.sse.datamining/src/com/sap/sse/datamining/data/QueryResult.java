package com.sap.sse.datamining.data;

import com.sap.sse.datamining.shared.AdditionalResultData;
import com.sap.sse.datamining.shared.QueryResultBase;

public interface QueryResult<ResultType> extends QueryResultBase<ResultType> {
    
    public Class<ResultType> getResultType();
    
    public AdditionalResultData getAdditionalData();

}
