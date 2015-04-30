package com.sap.sse.datamining;

import com.sap.sse.datamining.impl.NullDataMiningQueryManager;
import com.sap.sse.datamining.shared.DataMiningSession;
import com.sap.sse.datamining.shared.QueryResult;

public interface DataMiningQueryManager {
    
    public static final DataMiningQueryManager NULL = new NullDataMiningQueryManager(); 
    
    public <ResultType> QueryResult<ResultType> runNewAndAbortPrevious(DataMiningSession session, Query<ResultType> query);

}
