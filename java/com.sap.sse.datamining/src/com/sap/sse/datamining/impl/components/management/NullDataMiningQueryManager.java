package com.sap.sse.datamining.impl.components.management;

import com.sap.sse.datamining.Query;
import com.sap.sse.datamining.components.management.DataMiningQueryManager;
import com.sap.sse.datamining.shared.DataMiningSession;
import com.sap.sse.datamining.shared.QueryResult;

public class NullDataMiningQueryManager implements DataMiningQueryManager {

    @Override
    public <ResultType> QueryResult<ResultType> runNewAndAbortPrevious(DataMiningSession session, Query<ResultType> query) {
        return query.run();
    }

}
