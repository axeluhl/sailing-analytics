package com.sap.sailing.racecommittee.app.ui.comparators;

import com.sap.sailing.domain.base.Boat;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sse.common.Util;
import com.sap.sse.common.util.NaturalComparator;

import java.util.Comparator;
import java.util.Map;

public class CompetitorSailIdComparator implements Comparator<Map.Entry<Competitor, Boat>> {

    private Comparator<String> comparator;

    public CompetitorSailIdComparator() {
        this.comparator = new NaturalComparator();
    }

    @Override
    public int compare(Map.Entry<Competitor, Boat> leftCompetitor, Map.Entry<Competitor, Boat> rightCompetitor) {
        if (leftCompetitor != null && leftCompetitor.getValue() != null && rightCompetitor != null
                && rightCompetitor.getValue() != null) {
            Boat left = leftCompetitor.getValue();
            Boat right = rightCompetitor.getValue();
            String leftSailId = left == null ? null : left.getSailID();
            String rightSailId = right == null ? null : right.getSailID();
            return compare(leftSailId, rightSailId, comparator);
        }
        return 0;
    }

    public static int compare(String leftSailId, String rightSailId, Comparator<String> comparator) {
        if (leftSailId == null && rightSailId == null) {
            return 0;
        }
        if (leftSailId == null) {
            return -1;
        }
        if (rightSailId == null) {
            return 1;
        }
        String leftBoat = "";
        for (String lh : Util.splitAlongWhitespaceRespectingDoubleQuotedPhrases(leftSailId)) {
            leftBoat = lh;
        }
        String rightBoat = "";
        for (String rh : Util.splitAlongWhitespaceRespectingDoubleQuotedPhrases(rightSailId)) {
            rightBoat = rh;
        }
        return comparator.compare(leftBoat, rightBoat);
    }
}
