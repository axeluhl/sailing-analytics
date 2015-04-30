package com.sap.sse.datamining.impl;

import java.util.HashMap;
import java.util.Map;

import com.sap.sse.datamining.DataMiningQueryManager;
import com.sap.sse.datamining.Query;
import com.sap.sse.datamining.Query.QueryType;
import com.sap.sse.datamining.shared.DataMiningSession;
import com.sap.sse.datamining.shared.QueryResult;

public class StrategyPerQueryTypeManager implements DataMiningQueryManager {
    
    private final Map<QueryType, DataMiningQueryManager> managersMappedByQueryType;

    public StrategyPerQueryTypeManager() {
        managersMappedByQueryType = new HashMap<>();
        managersMappedByQueryType.put(QueryType.STATISTIC, new SingleQueryPerSessionManager());
        managersMappedByQueryType.put(QueryType.DIMENSION_VALUES, new SingleQueryPerDimensionManager());
        managersMappedByQueryType.put(QueryType.OTHER, NULL);
    }

    @Override
    public <ResultType> QueryResult<ResultType> runNewAndAbortPrevious(DataMiningSession session, Query<ResultType> query) {
        return managersMappedByQueryType.get(query.getAdditionalData().getType()).runNewAndAbortPrevious(session, query);
    }

}
