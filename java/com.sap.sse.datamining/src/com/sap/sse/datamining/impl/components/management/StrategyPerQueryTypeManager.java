package com.sap.sse.datamining.impl.components.management;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import com.sap.sse.datamining.Query;
import com.sap.sse.datamining.Query.QueryType;
import com.sap.sse.datamining.components.management.DataMiningQueryManager;
import com.sap.sse.datamining.data.QueryResult;
import com.sap.sse.datamining.shared.DataMiningSession;

public class StrategyPerQueryTypeManager implements DataMiningQueryManager {

    private final Map<QueryType, DataMiningQueryManager> managersMappedByQueryType;
    private final AtomicInteger runningQueryCounter;
    
    public StrategyPerQueryTypeManager() {
        managersMappedByQueryType = new HashMap<>();
        managersMappedByQueryType.put(QueryType.STATISTIC, new SingleQueryPerSessionManager());
        managersMappedByQueryType.put(QueryType.DIMENSION_VALUES, new SingleQueryPerDimensionManager());
        managersMappedByQueryType.put(QueryType.OTHER, NULL);
        runningQueryCounter = new AtomicInteger();
    }

    @Override
    public <ResultType> QueryResult<ResultType> runNewAndAbortPrevious(DataMiningSession session, Query<ResultType> query) {
        runningQueryCounter.incrementAndGet();
        final QueryResult<ResultType> result = managersMappedByQueryType.get(query.getAdditionalData().getType()).runNewAndAbortPrevious(session, query);
        runningQueryCounter.decrementAndGet();
        return result;
    }
    
    @Override
    public void abortRandomQuery() {
        Random generator = new Random();
        List<QueryType> queryTypes = new ArrayList<>(QueryType.values().length);
        Collections.addAll(queryTypes, QueryType.values());
        boolean abortedQuery = false;
        while (!abortedQuery) {
            QueryType randomQueryType = queryTypes.remove(generator.nextInt(queryTypes.size()));
            if (getNumberOfRunningQueriesOfType(randomQueryType) > 0) {
                abortRandomQueryOfType(randomQueryType);
                abortedQuery = true;
            }
        }
    }

    public void abortRandomQueryOfType(QueryType queryType) {
        managersMappedByQueryType.get(queryType).abortRandomQuery();
    }

    @Override
    public void abortAllQueries() {
        for (DataMiningQueryManager manager : managersMappedByQueryType.values()) {
            manager.abortAllQueries();
        }
    }
    
    @Override
    public int getNumberOfRunningQueries() {
        return runningQueryCounter.get();
    }

    public int getNumberOfRunningQueriesOfType(QueryType queryType) {
        return managersMappedByQueryType.get(queryType).getNumberOfRunningQueries();
    }

}
