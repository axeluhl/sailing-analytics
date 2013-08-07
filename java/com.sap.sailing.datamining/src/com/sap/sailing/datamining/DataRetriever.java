package com.sap.sailing.datamining;

import java.util.Collection;

import com.sap.sailing.server.RacingEventService;

public interface DataRetriever<DataType> {

    public Collection<DataType> retrieveData(RacingEventService racingEventService);
    
}
