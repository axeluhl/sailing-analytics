package com.sap.sailing.datamining;

import com.sap.sailing.datamining.shared.QueryResult;
import com.sap.sailing.server.RacingEventService;

public interface Query {
    
    public Selector getSelector();
    
    public QueryResult run(RacingEventService racingEventService);

}
