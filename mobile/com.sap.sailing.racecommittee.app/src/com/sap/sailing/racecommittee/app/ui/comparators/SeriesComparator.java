package com.sap.sailing.racecommittee.app.ui.comparators;

import java.util.Comparator;

import com.sap.sailing.racecommittee.app.ui.adapters.racelist.BoatClassSeriesDataFleet;

public class SeriesComparator implements Comparator<BoatClassSeriesDataFleet> {

    public int compare(BoatClassSeriesDataFleet lhs, BoatClassSeriesDataFleet rhs) {
        int result;
        int boatClassResult = lhs.getBoatClassName().compareTo(rhs.getBoatClassName());
        if (boatClassResult == 0) {
            int seriesResult = lhs.getSeriesName().compareTo(rhs.getSeriesName());
            if (seriesResult == 0) {
                result = lhs.getFleetName().compareTo(rhs.getFleetName());
            } else {
                result = seriesResult;
            }
        } else {
            result = boatClassResult;
        }
        
        return result;
    }
}
