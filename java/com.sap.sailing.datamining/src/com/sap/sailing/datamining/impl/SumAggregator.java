package com.sap.sailing.datamining.impl;

import java.util.Collection;
import java.util.Iterator;

import com.sap.sailing.datamining.Aggregator;

public abstract class SumAggregator<ExtractedType> implements Aggregator<ExtractedType, Number> {

    @Override
    public Number aggregate(Collection<ExtractedType> data) {
        Iterator<ExtractedType> dataIterator = data.iterator();
        Number sum = 0;

        while (dataIterator.hasNext()) {
            sum = add(sum, getNumericValue(dataIterator.next()));
        }
        return sum;
    }

    protected abstract Number add(Number number1, Number number2);
    protected abstract Number getNumericValue(ExtractedType value);

}
