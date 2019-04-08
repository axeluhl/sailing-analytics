package com.sap.sailing.polars.datamining.shared;

import com.sap.sse.datamining.shared.SerializationDummy;

/**
 * Dummy class for including some DTOs in the GWT serialization policy. The datamining framework is so generic that
 * classes are not automatically included in the policy, even though they are needed. That is why this dummy has to be
 * created.
 * 
 * @author D054528 (Frederik Petersen)
 *
 */
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
