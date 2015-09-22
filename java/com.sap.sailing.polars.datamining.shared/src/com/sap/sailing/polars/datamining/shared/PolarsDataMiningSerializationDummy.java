package com.sap.sailing.polars.datamining.shared;

import com.sap.sse.datamining.shared.SerializationDummy;

@SuppressWarnings("unused")
public final class PolarsDataMiningSerializationDummy implements SerializationDummy {
    private static final long serialVersionUID = 2L;
    
    private PolarAggregation polarAggregation;
    private PolarAggregationImpl polarAggregationImpl;
    private PolarBackendData polarBackendAggregation;
    private PolarBackendDataImpl polarBackendAggregationImpl;
    private Boolean itsABoolean;
    
    private PolarsDataMiningSerializationDummy() { }
    
}
