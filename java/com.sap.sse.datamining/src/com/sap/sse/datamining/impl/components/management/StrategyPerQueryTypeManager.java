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
        // here is a possibility to provide different query managers for different types of queries;
        // also see bug 5813; there used to be different managers trying to allow only one query per
        // scope, but the problem was that within a single session multiple queries could need to run
        // in parallel, and server-side we currently have no good way to identify two Query objects as
        // referring to the same query on the client side because there is no stable ID on those Query
        // objects. The same goes for Dimension queries where different queries may use equal dimensions
        // that here couldn't be told apart. So currently we're not using any aborting query managers
        // but instead rely on resource consumption-based query aborting.
        managersMappedByQueryType = new HashMap<>();
        managersMappedByQueryType.put(QueryType.OTHER, NULL);
        managersMappedByQueryType.put(QueryType.STATISTIC, NULL);
        managersMappedByQueryType.put(QueryType.DIMENSION_VALUES, NULL);
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
