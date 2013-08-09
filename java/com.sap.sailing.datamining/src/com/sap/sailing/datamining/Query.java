package com.sap.sailing.datamining;

import com.sap.sailing.datamining.shared.QueryResult;
import com.sap.sailing.server.RacingEventService;

public interface Query<DataType, AggregatedType> {
    
    public QueryResult<AggregatedType> run(RacingEventService racingEventService);

}
