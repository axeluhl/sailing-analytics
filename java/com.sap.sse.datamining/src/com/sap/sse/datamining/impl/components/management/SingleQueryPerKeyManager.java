package com.sap.sse.datamining.impl.components.management;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Logger;

import com.sap.sse.datamining.Query;
import com.sap.sse.datamining.QueryState;
import com.sap.sse.datamining.components.management.DataMiningQueryManager;
import com.sap.sse.datamining.data.QueryResult;
import com.sap.sse.datamining.shared.DataMiningSession;

public abstract class SingleQueryPerKeyManager<T> implements DataMiningQueryManager {

    private final Logger logger = Logger.getLogger(this.getClass().getName());

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
        logger.info("Running query " + query + ", that has been registered for the keys " + keys);
        QueryResult<ResultType> result = query.run();
        unregisterQuery(keys, query);
        return result;
    }
    
    @Override
    public void abortRandomQuery() {
        Random generator = new Random();
        boolean abortedQuery = false;
        final Set<T> keySet = queryMap.keySet(); // Changes to the map will be reflected to this set
        while (!abortedQuery) {
            List<T> keys = new ArrayList<>(keySet);
            T randomKey = keys.get(generator.nextInt(keys.size()));
            abortedQuery = abortQuery(randomKey);
        }
    }
    
    @Override
    public void abortAllQueries() {
        Collection<Entry<T, Query<?>>> queries = new HashSet<>(queryMap.entrySet());
        queryMap.clear();
        queries.parallelStream().forEach(element -> {
            T key = element.getKey();
            Query<?> query = element.getValue();
            logger.info("Aborting query " + query + " for the key " + key);
            query.abort();
        });
    }
    
    @Override
    public int getNumberOfRunningQueries() {
        return queryMap.size();
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
                abortQuery(key);
            }
        }
    }

    private boolean abortQuery(T key) {
        boolean abortedQuery = false;
        Query<?> query = queryMap.get(key);
        if (query.getState() == QueryState.RUNNING) {
            logger.info("Aborting query " + query + " for the key " + key);
            query.abort();
            abortedQuery = true;
        }
        queryMap.remove(key, query);
        return abortedQuery;
    }

    private <ResultType> void registerNewQuery(Iterable<T> keys, Query<ResultType> query) {
        for (T key : keys) {
            Query<?> previousValue = queryMap.putIfAbsent(key, query);
            if (previousValue != null) {
                throw new UnsupportedOperationException("There's allready a query for the key: " + key);
            }
        }
    }

    private void unregisterQuery(Iterable<T> keys, Query<?> finishedQuery) {
        for (T key : keys) {
            queryMap.remove(key, finishedQuery);
        }
    }

}
