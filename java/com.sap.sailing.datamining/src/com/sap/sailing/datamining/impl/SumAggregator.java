package com.sap.sailing.datamining.impl;

import java.util.Collection;
import java.util.Iterator;

public abstract class SumAggregator<ExtractedType, AggregatedType> extends AbstractAggregator<ExtractedType, AggregatedType> {

    public SumAggregator() {
        super("Sum");
    }

    @Override
    public AggregatedType aggregate(Collection<ExtractedType> data) {
        Iterator<ExtractedType> dataIterator = data.iterator();
        AggregatedType sum = null;

        while (dataIterator.hasNext()) {
            if (sum == null) {
                sum = getValueFor(dataIterator.next());
            }
            
            sum = add(sum, getValueFor(dataIterator.next()));
        }
        return sum;
    }

    protected abstract AggregatedType add(AggregatedType value1, AggregatedType value2);
    protected abstract AggregatedType getValueFor(ExtractedType extractedValue);

}
