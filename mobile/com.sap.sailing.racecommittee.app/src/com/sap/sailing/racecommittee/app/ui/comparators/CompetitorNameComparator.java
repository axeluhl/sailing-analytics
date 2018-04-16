package com.sap.sailing.racecommittee.app.ui.comparators;

import java.util.Comparator;
import java.util.Map;

import com.sap.sailing.domain.base.Boat;
import com.sap.sailing.domain.base.Competitor;

public class CompetitorNameComparator implements Comparator<Map.Entry<Competitor, Boat>> {

    private NaturalNamedComparator<Competitor> comparator;

    public CompetitorNameComparator() {
        this.comparator = new NaturalNamedComparator<>();
    }

    @Override
    public int compare(Map.Entry<Competitor, Boat> leftCompetitor, Map.Entry<Competitor, Boat> rightCompetitor) {
        if (leftCompetitor != null && leftCompetitor.getKey() != null &&
            rightCompetitor != null && rightCompetitor.getKey() != null) {
            Competitor left = leftCompetitor.getKey();
            Competitor right = rightCompetitor.getKey();

            return comparator.compare(left, right);
        }
        return 0;
    }
}
