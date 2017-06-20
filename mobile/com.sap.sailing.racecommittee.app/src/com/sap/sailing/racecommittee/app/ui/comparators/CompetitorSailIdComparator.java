package com.sap.sailing.racecommittee.app.ui.comparators;

import java.util.Comparator;

import com.sap.sailing.domain.base.Boat;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.CompetitorWithBoat;
import com.sap.sse.common.util.NaturalComparator;

public class CompetitorSailIdComparator implements Comparator<Competitor> {

    private Comparator<String> comparator;

    public CompetitorSailIdComparator() {
        this.comparator = new NaturalComparator();
    }

    @Override
    public int compare(Competitor leftCompetitor, Competitor rightCompetitor) {
        if (leftCompetitor != null && leftCompetitor instanceof CompetitorWithBoat &&
            rightCompetitor != null && rightCompetitor instanceof CompetitorWithBoat) {
            Boat leftBoat = ((CompetitorWithBoat) leftCompetitor).getBoat();
            Boat rightBoat = ((CompetitorWithBoat) rightCompetitor).getBoat();
            
            // if left is null, right will be greater
            if (leftBoat == null || leftBoat.getSailID() == null) {
                return -1;
            }

            // if right is null, left will be greater
            if (rightBoat == null || rightBoat.getSailID() == null) {
                return 1;
            }

            return comparator.compare(leftBoat.getSailID(), rightBoat.getSailID());
        }
        return 0;
    }
}
