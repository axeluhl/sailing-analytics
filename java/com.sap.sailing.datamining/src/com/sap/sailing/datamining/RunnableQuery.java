package com.sap.sailing.datamining;

import java.util.List;

import com.sap.sailing.datamining.shared.Query;
import com.sap.sailing.domain.common.impl.Util.Pair;
import com.sap.sailing.server.RacingEventService;

public interface RunnableQuery extends Query {
    
    public List<Pair<String, Double>> run(RacingEventService racingEventService);

}
