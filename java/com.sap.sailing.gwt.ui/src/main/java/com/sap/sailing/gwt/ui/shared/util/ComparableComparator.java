package com.sap.sailing.gwt.ui.shared.util;

import java.util.Comparator;

public class ComparableComparator<T extends Comparable<T>> implements Comparator<T> {

    @Override
    public int compare(T left, T right) {
        if (left == right) {
            return 0;
        }
        return left.compareTo(right);
    }

}
