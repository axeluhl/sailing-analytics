package com.sap.sailing.gwt.ui.shared.util;

import java.util.Comparator;

public class NullSafeComparatorWrapper<T> implements Comparator<T> {

    private final boolean sortNullToTop;
    private final Comparator<T> comparator;

    public NullSafeComparatorWrapper(Comparator<T> comparator) {
        this(comparator, false);
    }

    public NullSafeComparatorWrapper(Comparator<T> comparator, boolean sortNullToTop) {
        this.comparator = comparator;
        this.sortNullToTop = sortNullToTop;
    }

    @Override
    public int compare(T left, T right) {
        if (left == right) {
            return 0;
        }
        if (left == null) {
            return sortNullToTop ? -1 : 1;
        }
        if (right == null) {
            return sortNullToTop ? 1 : -1;
        }
        return comparator.compare(left, right);
    }

}
