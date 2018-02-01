package com.sap.sailing.racecommittee.app.ui.comparators;

import java.util.Comparator;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sse.common.Util;
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

        String leftBoat = "";
        for (String lh : Util.splitAlongWhitespaceRespectingDoubleQuotedPhrases(left.getBoat().getSailID())) {
            leftBoat = lh;
        }
        String rightBoat = "";
        for (String rh : Util.splitAlongWhitespaceRespectingDoubleQuotedPhrases(right.getBoat().getSailID())) {
            rightBoat = rh;
        }
        return comparator.compare(leftBoat, rightBoat);
    }
}
