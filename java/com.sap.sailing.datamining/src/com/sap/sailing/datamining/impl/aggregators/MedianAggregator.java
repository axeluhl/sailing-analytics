package com.sap.sailing.datamining.impl.aggregators;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public abstract class MedianAggregator<ExtractedType extends Comparable<? super ExtractedType>, AggregatedType> extends AbstractAggregator<ExtractedType, AggregatedType> {

    public MedianAggregator() {
        super("Median");
    }

    @Override
    public AggregatedType aggregate(Collection<ExtractedType> data) {
        List<ExtractedType> dataAsList = new ArrayList<ExtractedType>(data);
        Collections.sort(dataAsList);
        
        if (lengthIsEven(dataAsList)) {
            AggregatedType value1 = getAggregatedValueFor(dataAsList.get((dataAsList.size() - 1) / 2));
            AggregatedType value2 = getAggregatedValueFor(dataAsList.get(((dataAsList.size() - 1) / 2) + 1));
            return divideByTwo(add(value1, value2));
        } else {
            return getAggregatedValueFor(dataAsList.get((dataAsList.size()) / 2));
        }
    }

    private boolean lengthIsEven(Collection<ExtractedType> data) {
        return data.size() % 2 == 0;
    }

    protected abstract AggregatedType getAggregatedValueFor(ExtractedType extractedType);
    protected abstract AggregatedType add(AggregatedType value1, AggregatedType value2);
    protected abstract AggregatedType divideByTwo(AggregatedType value);

}
