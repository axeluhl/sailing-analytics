package com.sap.sse.datamining.impl;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.sap.sse.datamining.DataMiningQueryManager;
import com.sap.sse.datamining.Query;
import com.sap.sse.datamining.QueryState;
import com.sap.sse.datamining.shared.DataMiningSession;
import com.sap.sse.datamining.shared.QueryResult;

public abstract class SingleQueryPerKeyManager<T> implements DataMiningQueryManager {

    private final ConcurrentMap<T, Query<?>> queryMap;

    public SingleQueryPerKeyManager() {
        queryMap = new ConcurrentHashMap<>();
    }

    @Override
    public <ResultType> QueryResult<ResultType> runNewAndAbortPrevious(DataMiningSession session, Query<ResultType> query) {
        if (session == null || query == null) {
            //Forbidding null Queries ensures the functionality of registerNewQuery
            throw new NullPointerException();
        }
        
        Iterable<T> keys = getKeysFor(session, query);
        validate(keys);
        
        abortPreviousQueries(keys);
        registerNewQuery(keys, query);
        QueryResult<ResultType> result = query.run();
        unregisterQuery(keys, query);
        return result;
    }

    protected abstract <ResultType> Iterable<T> getKeysFor(DataMiningSession session, Query<ResultType> query);
    
    private void validate(Iterable<T> keys) {
        int size = 0;
        for (T key : keys) {
            if (key == null) {
                throw new IllegalArgumentException("Null key was created for query.");
            }
            size++;
        }
        
        if (size == 0) {
            throw new IllegalArgumentException("Unable to create keys for query.");
        }
    }
    
    private void abortPreviousQueries(Iterable<T> keys) {
        for (T key : keys) {
            if (queryMap.containsKey(key)) {
                Query<?> previousQuery = queryMap.get(key);
                if (previousQuery.getState() == QueryState.RUNNING) {
                    previousQuery.abort();
                }
                queryMap.remove(key, previousQuery);
            }
        }
    }

    private <ResultType> void registerNewQuery(Iterable<T> keys, Query<ResultType> query) {
        for (T key : keys) {
            Query<?> previousValue = queryMap.putIfAbsent(key, query);
            if (previousValue != null) {
                throw new UnsupportedOperationException("There's allready a Query for the session: " + key);
            }
        }
    }

    private void unregisterQuery(Iterable<T> keys, Query<?> finishedQuery) {
        for (T key : keys) {
            queryMap.remove(key, finishedQuery);
        }
    }

}
