package com.sap.sse.datamining.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.sap.sse.datamining.DataMiningQueryManager;
import com.sap.sse.datamining.Query;
import com.sap.sse.datamining.shared.DataMiningSession;
import com.sap.sse.datamining.shared.QueryResult;

public class ManyQueriesPerRetrieverChainManager implements DataMiningQueryManager {

    private final ConcurrentMap<DataMiningSession, UUID> currentRetrieverChainMappedBySession;
    private final Map<DataMiningSession, Collection<Query<?>>> queriesMappedBySession;

    public ManyQueriesPerRetrieverChainManager() {
        currentRetrieverChainMappedBySession = new ConcurrentHashMap<>();
        queriesMappedBySession = new HashMap<>();
    }

    @Override
    public <ResultType> QueryResult<ResultType> runNewAndAbortPrevious(DataMiningSession session, Query<ResultType> query) {
        if (session == null || query == null) {
            throw new NullPointerException();
        }
        
        abortPreviousQueries(session, query);
        updateCurrentRetrieverChain(session, query);
        registerNewQuery(session, query);
        QueryResult<ResultType> result = query.run();
        unregisterQuery(session, query);
        return result;
    }
    
    private void abortPreviousQueries(DataMiningSession session, Query<?> newQuery) {
        final UUID newRetrieverChainID = newQuery.getAdditionalData().getDataRetrieverChainID();
        if (currentRetrieverChainMappedBySession.containsKey(session) &&
                !currentRetrieverChainMappedBySession.get(session).equals(newRetrieverChainID)) {
            Collection<Query<?>> previousQueries = queriesMappedBySession.get(session);
            for (Query<?> previousQuery : previousQueries) {
                previousQuery.abort();
            }
            queriesMappedBySession.remove(session, previousQueries);
            
            currentRetrieverChainMappedBySession.remove(session);
            currentRetrieverChainMappedBySession.putIfAbsent(session, newRetrieverChainID);
        }
    }

    private void updateCurrentRetrieverChain(DataMiningSession session, Query<?> query) {
        UUID retrieverChainID = query.getAdditionalData().getDataRetrieverChainID();
        UUID previousRetrieverChainID = currentRetrieverChainMappedBySession.putIfAbsent(session, retrieverChainID);
        if (previousRetrieverChainID != null && !previousRetrieverChainID.equals(retrieverChainID)) {
            currentRetrieverChainMappedBySession.replace(session, previousRetrieverChainID, retrieverChainID);
        }
    }

    private <ResultType> void registerNewQuery(DataMiningSession session, Query<ResultType> query) {
        if (!queriesMappedBySession.containsKey(session)) {
            Set<Query<?>> concurrentSet = Collections.newSetFromMap(new ConcurrentHashMap<Query<?>, Boolean>());
            queriesMappedBySession.put(session, concurrentSet);
        }
        
        queriesMappedBySession.get(session).add(query);
    }

    private void unregisterQuery(DataMiningSession session, Query<?> finishedQuery) {
        UUID retrieverChainID = finishedQuery.getAdditionalData().getDataRetrieverChainID();
        if (currentRetrieverChainMappedBySession.containsKey(session) &&
                currentRetrieverChainMappedBySession.get(session).equals(retrieverChainID)) {
            queriesMappedBySession.get(session).remove(finishedQuery);
        }
    }

}
