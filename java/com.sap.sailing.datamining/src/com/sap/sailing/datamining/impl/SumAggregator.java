package com.sap.sailing.datamining.impl;

import java.util.Collection;
import java.util.Iterator;

import com.sap.sailing.datamining.Aggregator;

public abstract class SumAggregator<ExtractedType, AggregatedType> implements Aggregator<ExtractedType, AggregatedType> {

    @Override
    public AggregatedType aggregate(Collection<ExtractedType> data) {
        Iterator<ExtractedType> dataIterator = data.iterator();
        AggregatedType sum = null;

        while (dataIterator.hasNext()) {
            sum = add(sum, getValueFor(dataIterator.next()));
        }
        return sum;
    }

    protected abstract AggregatedType add(AggregatedType value1, AggregatedType value2);
    protected abstract AggregatedType getValueFor(ExtractedType extractedValue);

}
