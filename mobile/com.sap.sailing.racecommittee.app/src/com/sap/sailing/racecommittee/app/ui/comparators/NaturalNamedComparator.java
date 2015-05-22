package com.sap.sailing.racecommittee.app.ui.comparators;

import com.sap.sailing.domain.common.impl.NaturalComparator;
import com.sap.sse.common.Named;

import java.util.Comparator;

public class NaturalNamedComparator implements Comparator<Named> {

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
    public int compare(Named left, Named right) {
        return comparator.compare(left.getName(), right.getName());
    }

}