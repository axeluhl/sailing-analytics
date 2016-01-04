package com.sap.sse.datamining.impl.components.management;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import com.sap.sse.datamining.Query;
import com.sap.sse.datamining.components.management.DataMiningQueryManager;
import com.sap.sse.datamining.data.QueryResult;
import com.sap.sse.datamining.shared.DataMiningSession;

public class NullDataMiningQueryManager implements DataMiningQueryManager {

    private final Logger logger = Logger.getLogger(this.getClass().getName());
    
    private final Set<Query<?>> queries = Collections.newSetFromMap(new ConcurrentHashMap<>());

    @Override
    public <ResultType> QueryResult<ResultType> runNewAndAbortPrevious(DataMiningSession session, Query<ResultType> query) {
        logger.info("This query manager, doesn't manage anything. Just running the query " + query);
        queries.add(query);
        final QueryResult<ResultType> result = query.run();
        queries.remove(query);
        return result;
    }
    
    @Override
    public void abortRandomQuery() {
        Random generator = new Random();
        List<Query<?>> queries = new ArrayList<>(this.queries);
        Query<?> query = queries.get(generator.nextInt(queries.size()));
        abortQuery(query);
    }
    
    @Override
    public void abortAllQueries() {
        Collection<Query<?>> queries = new HashSet<>(this.queries);
        this.queries.clear();
        queries.forEach(query -> abortQuery(query));
    }

    private void abortQuery(Query<?> query) {
        logger.info("Aborting query " + query);
        query.abort();
    }

    @Override
    public int getNumberOfRunningQueries() {
        return queries.size();
    }

}
