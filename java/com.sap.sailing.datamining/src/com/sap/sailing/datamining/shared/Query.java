package com.sap.sailing.datamining.shared;

import java.io.Serializable;

public interface Query extends Serializable {
    
    public Selector getSelector();
    public Extractor getExtractor();
    public Aggregator getAggregator();

}
