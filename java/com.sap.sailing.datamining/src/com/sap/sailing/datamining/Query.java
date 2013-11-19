package com.sap.sailing.datamining;

import java.util.concurrent.ExecutionException;

import com.sap.sailing.datamining.shared.QueryResult;
import com.sap.sailing.server.RacingEventService;

public interface Query<DataType, AggregatedType> {
    
    public QueryResult<AggregatedType> run(RacingEventService racingEventService) throws InterruptedException, ExecutionException;

}
