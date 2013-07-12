package com.sap.sailing.datamining.impl;

import com.sap.sailing.datamining.shared.Aggregator;
import com.sap.sailing.datamining.shared.Extractor;
import com.sap.sailing.datamining.shared.Query;
import com.sap.sailing.datamining.shared.Selector;

public class QueryImpl implements Query {
    private static final long serialVersionUID = 5649156014930954522L;
    
    private Selector selector;
    private Extractor extractor;
    private Aggregator aggregator;

    public QueryImpl(Selector selector, Extractor extractor, Aggregator aggregator) {
        this.selector = selector;
        this.extractor = extractor;
        this.aggregator = aggregator;
    }

    @Override
    public Selector getSelector() {
        return selector;
    }

    @Override
    public Extractor getExtractor() {
        return extractor;
    }

    @Override
    public Aggregator getAggregator() {
        return aggregator;
    }

}
