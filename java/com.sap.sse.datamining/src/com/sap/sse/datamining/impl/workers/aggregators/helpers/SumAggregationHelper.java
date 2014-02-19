package com.sap.sse.datamining.impl.workers.aggregators.helpers;

import java.util.Collection;
import java.util.Iterator;

public abstract class SumAggregationHelper<ExtractedType, AggregatedType> {

    public AggregatedType aggregate(Collection<ExtractedType> data) {
        Iterator<ExtractedType> dataIterator = data.iterator();
        AggregatedType sum = dataIterator.hasNext() ? getValueFor(dataIterator.next()) : null;

        while (dataIterator.hasNext()) {
            sum = add(sum, getValueFor(dataIterator.next()));
        }
        return sum;
    }

    protected abstract AggregatedType add(AggregatedType value1, AggregatedType value2);
    protected abstract AggregatedType getValueFor(ExtractedType extractedValue);

}
