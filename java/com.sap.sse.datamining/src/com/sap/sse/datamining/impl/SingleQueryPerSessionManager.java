package com.sap.sse.datamining.impl;

import java.util.Collection;
import java.util.HashSet;

import com.sap.sse.datamining.Query;
import com.sap.sse.datamining.shared.DataMiningSession;

public class SingleQueryPerSessionManager extends SingleQueryPerKeyManager<DataMiningSession> {

    @Override
    protected <ResultType> Iterable<DataMiningSession> getKeysFor(DataMiningSession session, Query<ResultType> query) {
        Collection<DataMiningSession> keys = new HashSet<>();
        keys.add(session);
        return keys;
    }
    
}
