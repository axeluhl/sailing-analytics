package com.sap.sailing.datamining.impl;

import java.util.Collection;

import com.sap.sailing.datamining.Aggregator;
import com.sap.sailing.domain.confidence.AbstractScalarValue;
import com.sap.sailing.domain.confidence.ScalableValue;

public class AverageAggregator<ValueType, AveragesTo> implements Aggregator<ValueType, AveragesTo>  {

    @Override
    public ScalableValue<ValueType, AveragesTo> aggregate(Collection<ScalableValue<ValueType, AveragesTo>> data) {
        AveragesTo average = new SumAggregator<ValueType, AveragesTo>().aggregate(data).divide(data.size());
        return average;
    }

}
