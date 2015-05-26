package com.sap.sailing.gwt.ui.shared.util;

import java.util.Comparator;

public class NullSafeComparableComparator<T extends Comparable<T>> implements Comparator<T> {

    private final boolean sortNullToTop;

    public NullSafeComparableComparator() {
        this(false);
    }

    public NullSafeComparableComparator(boolean sortNullToTop) {
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
        return left.compareTo(right);
    }

}
