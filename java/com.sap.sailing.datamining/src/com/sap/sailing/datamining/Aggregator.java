package com.sap.sailing.datamining;

import java.util.Collection;

import com.sap.sailing.domain.confidence.ScalableValue;

public interface Aggregator<ValueType, AveragesTo> {
    
    public ScalableValue<ValueType, AveragesTo> aggregate(Collection<ScalableValue<ValueType, AveragesTo>> data);

}
