package com.sap.sailing.gwt.home.desktop.partials.racelist;

import java.util.Comparator;

import com.sap.sse.common.impl.InvertibleComparatorAdapter;

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
