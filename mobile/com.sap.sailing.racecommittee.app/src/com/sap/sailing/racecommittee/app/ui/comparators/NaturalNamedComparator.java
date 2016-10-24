package com.sap.sailing.racecommittee.app.ui.comparators;

import java.util.Comparator;

import com.sap.sse.common.Named;
import com.sap.sse.common.util.NaturalComparator;

public class NaturalNamedComparator<T extends Named> implements Comparator<T> {

    private Comparator<String> comparator;

    public NaturalNamedComparator() {
        this.comparator = new NaturalComparator();
    }

    /**
     * compares names naturally and returns the ordinal comparison result
     * 
     * @param left
     * @param right
     * @return the comparison result (see String.compareTo)
     */
    @Override
    public int compare(T left, T right) {
        return comparator.compare(left.getName(), right.getName());
    }

}