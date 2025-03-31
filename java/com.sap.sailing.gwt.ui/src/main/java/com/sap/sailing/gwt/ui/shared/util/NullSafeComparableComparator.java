package com.sap.sailing.gwt.ui.shared.util;

import java.util.Comparator;

public class NullSafeComparableComparator<T extends Comparable<T>> implements Comparator<T> {

    private final Comparator<T> comparator;

    public NullSafeComparableComparator() {
        this(false);
    }

    public NullSafeComparableComparator(boolean sortNullToTop) {
        comparator = new NullSafeComparatorWrapper<>(new ComparableComparator<T>(), sortNullToTop);
    }

    @Override
    public int compare(T left, T right) {
        return comparator.compare(left, right);
    }

}
