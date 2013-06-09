package com.sap.sailing.racecommittee.app.ui.comparators;

import java.util.Comparator;

import com.sap.sailing.domain.common.impl.NaturalComparator;
import com.sap.sailing.racecommittee.app.ui.adapters.racelist.BoatClassSeriesDataFleet;

public class BoatClassSeriesDataFleetComparator implements Comparator<BoatClassSeriesDataFleet> {

    private Comparator<String> nameComparator;

    public BoatClassSeriesDataFleetComparator() {
        this.nameComparator = new NaturalComparator();
    }

    public int compare(BoatClassSeriesDataFleet left, BoatClassSeriesDataFleet right) {
        int result = nameComparator.compare(left.getBoatClassName(), right.getBoatClassName());
        if (result == 0) {
            result = nameComparator.compare(left.getSeriesName(), right.getSeriesName());
            if (result == 0) {
                result = nameComparator.compare(left.getFleetName(), right.getFleetName());
            }
        }
        return result;
    }
}
