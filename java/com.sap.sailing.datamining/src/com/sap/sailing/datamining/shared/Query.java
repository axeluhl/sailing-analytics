package com.sap.sailing.datamining.shared;

public interface Query {
    
    public void setSelector(Selector selector);
    public Selector getSelector();
    
    public void setAggregator(Aggregator aggregator);
    public Aggregator getAggregator();

}
