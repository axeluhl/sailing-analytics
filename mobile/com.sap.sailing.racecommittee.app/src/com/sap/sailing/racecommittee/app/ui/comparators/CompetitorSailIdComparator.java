package com.sap.sailing.racecommittee.app.ui.comparators;

import java.util.Comparator;
import java.util.Map;

import com.sap.sailing.domain.base.Boat;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sse.common.Util;
import com.sap.sse.common.util.NaturalComparator;

public class CompetitorSailIdComparator implements Comparator<Map.Entry<Competitor, Boat>> {

    private Comparator<String> comparator;

    public CompetitorSailIdComparator() {
        this.comparator = new NaturalComparator();
    }

    @Override
    public int compare(Map.Entry<Competitor, Boat> leftCompetitor, Map.Entry<Competitor, Boat> rightCompetitor) {
        if (leftCompetitor != null && leftCompetitor.getValue() != null &&
            rightCompetitor != null && rightCompetitor.getValue() != null) {
            Boat left = leftCompetitor.getValue();
            Boat right = rightCompetitor.getValue();
            
            // if right is null, left will be greater
            if (right == null || right.getSailID() == null) {
                return 1;
            }

            String leftBoat = "";
            for (String lh : Util.splitAlongWhitespaceRespectingDoubleQuotedPhrases(left.getSailID())) {
                leftBoat = lh;
            }
            String rightBoat = "";
            for (String rh : Util.splitAlongWhitespaceRespectingDoubleQuotedPhrases(right.getSailID())) {
                rightBoat = rh;
            }
            return comparator.compare(leftBoat, rightBoat);
        }
        return 0;
    }
}
