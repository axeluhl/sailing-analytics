package com.sap.sailing.datamining;

import com.sap.sailing.datamining.impl.QueryImpl;

public class QueryFactory {
    
    public static Query createQuery(Selector selector, Extractor extractor, Aggregator aggregator) {
        return new QueryImpl(selector, extractor, aggregator);
    }
}
