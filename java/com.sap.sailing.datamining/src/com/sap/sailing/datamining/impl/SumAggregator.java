package com.sap.sailing.datamining.impl;

import java.util.Collection;
import java.util.Iterator;

import com.sap.sailing.datamining.Aggregator;
import com.sap.sailing.domain.confidence.ScalableValue;

public class SumAggregator<ValueType, AveragesTo> implements Aggregator<ValueType, AveragesTo> {

    @Override
    public ScalableValue<ValueType, AveragesTo> aggregate(Collection<ScalableValue<ValueType, AveragesTo>> data) {
        Iterator<ScalableValue<ValueType, AveragesTo>> dataIterator = data.iterator();
        ScalableValue<ValueType, AveragesTo> sum = null;
        
        if (dataIterator.hasNext()) {
            sum = dataIterator.next();
            while (dataIterator.hasNext()) {
                sum.add(dataIterator.next());
            }
        }
        return sum;
    }

}
