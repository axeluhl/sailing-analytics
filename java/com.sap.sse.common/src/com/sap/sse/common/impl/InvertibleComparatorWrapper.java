package com.sap.sse.common.impl;

import java.util.Comparator;

public abstract class InvertibleComparatorWrapper<T, C> extends InvertibleComparatorAdapter<T> {

    private final Comparator<C> wrappedComparator;

    public InvertibleComparatorWrapper(Comparator<C> wrappedComparator) {
        this.wrappedComparator = wrappedComparator;
    }

    @Override
    public final int compare(T left, T right) {
        return wrappedComparator.compare(getComparisonValue(left), getComparisonValue(right));
    }

    protected abstract C getComparisonValue(T object);

}
