package com.sap.sailing.datamining;

import java.util.Collection;

import com.sap.sailing.datamining.impl.ParallelComponent;
import com.sap.sailing.server.RacingEventService;

public interface ParallelDataRetriever<DataType> extends ParallelComponent<RacingEventService, Collection<DataType>> {
    
}
