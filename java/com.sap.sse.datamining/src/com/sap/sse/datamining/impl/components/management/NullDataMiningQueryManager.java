package com.sap.sse.datamining.impl.components.management;

import java.util.logging.Logger;

import com.sap.sse.datamining.Query;
import com.sap.sse.datamining.components.management.DataMiningQueryManager;
import com.sap.sse.datamining.data.QueryResult;
import com.sap.sse.datamining.shared.DataMiningSession;

public class NullDataMiningQueryManager implements DataMiningQueryManager {

    private final Logger logger = Logger.getLogger(this.getClass().getName());

    @Override
    public <ResultType> QueryResult<ResultType> runNewAndAbortPrevious(DataMiningSession session, Query<ResultType> query) {
        logger.info("This query manager, doesn't manage anything. Just running the query " + query);
        return query.run();
    }

}
