package com.sap.sailing.datamining;

import java.io.Serializable;
import java.util.List;

import com.sap.sailing.domain.common.impl.Util.Pair;
import com.sap.sailing.server.RacingEventService;

public interface Query extends Serializable {
    
    public Selector getSelector();
    public Extractor getExtractor();
    public Aggregator getAggregator();
    
    public List<Pair<String, Double>> run(RacingEventService racingEventService);

}
