package com.sap.sailing.racecommittee.app.ui.comparators;

import java.util.Comparator;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sse.common.util.NaturalComparator;

public class CompetitorSailIdComparator implements Comparator<Competitor> {

    private Comparator<String> comparator;

    public CompetitorSailIdComparator() {
        this.comparator = new NaturalComparator();
    }

    @Override
    public int compare(Competitor left, Competitor right) {
        // if left is null, right will be greater
        if (left == null || left.getBoat() == null || left.getBoat().getSailID() == null) {
            return -1;
        }

        // if right is null, left will be greater
        if (right == null || right.getBoat() == null || right.getBoat().getSailID() == null) {
            return 1;
        }

        return comparator.compare(left.getBoat().getSailID(), right.getBoat().getSailID());
    }
}
