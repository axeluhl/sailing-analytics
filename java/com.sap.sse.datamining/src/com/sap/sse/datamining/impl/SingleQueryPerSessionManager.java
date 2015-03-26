package com.sap.sse.datamining.impl;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.sap.sse.datamining.DataMiningQueryManager;
import com.sap.sse.datamining.Query;
import com.sap.sse.datamining.shared.DataMiningSession;
import com.sap.sse.datamining.shared.QueryResult;

public class SingleQueryPerSessionManager implements DataMiningQueryManager {

    private final ConcurrentMap<DataMiningSession, Query<?>> queryMappedBySession;

    public SingleQueryPerSessionManager() {
        queryMappedBySession = new ConcurrentHashMap<>();
    }

    @Override
    public <ResultType> QueryResult<ResultType> runNewAndAbortPrevious(DataMiningSession session, Query<ResultType> query) {
        if (session == null || query == null) {
            //Forbidding null Queries ensures the functionality of registerNewQuery
            throw new NullPointerException();
        }
        
        abortPreviousQueries(session);
        registerNewQuery(session, query);
        QueryResult<ResultType> result = query.run();
        unregisterQuery(session, query);
        return result;
    }
    
    private void abortPreviousQueries(DataMiningSession session) {
        if (queryMappedBySession.containsKey(session)) {
            Query<?> previousQuery = queryMappedBySession.get(session);
            previousQuery.abort();
            queryMappedBySession.remove(session, previousQuery);
        }
    }

    private <ResultType> void registerNewQuery(DataMiningSession session, Query<ResultType> query) {
        Query<?> previousValue = queryMappedBySession.putIfAbsent(session, query);
        if (previousValue != null) {
            throw new UnsupportedOperationException("There's allready a Query for the session: " + session);
        }
    }

    private void unregisterQuery(DataMiningSession session, Query<?> finishedQuery) {
        queryMappedBySession.remove(session, finishedQuery);
    }
    
}
