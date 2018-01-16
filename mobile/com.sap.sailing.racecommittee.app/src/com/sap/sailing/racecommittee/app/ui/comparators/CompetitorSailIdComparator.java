package com.sap.sailing.racecommittee.app.ui.comparators;

import java.util.Comparator;

import com.sap.sailing.domain.base.Boat;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.CompetitorWithBoat;
import com.sap.sse.common.Util;
import com.sap.sse.common.util.NaturalComparator;

public class CompetitorSailIdComparator implements Comparator<Competitor> {

    private Comparator<String> comparator;

    public CompetitorSailIdComparator() {
        this.comparator = new NaturalComparator();
    }

    @Override
    public int compare(Competitor leftCompetitor, Competitor rightCompetitor) {
        if (leftCompetitor != null && leftCompetitor.hasBoat() &&
            rightCompetitor != null && rightCompetitor.hasBoat()) {
            Boat left = ((CompetitorWithBoat) leftCompetitor).getBoat();
            Boat right = ((CompetitorWithBoat) rightCompetitor).getBoat();
            
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
