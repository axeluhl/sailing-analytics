package com.sap.sailing.racecommittee.app.ui.comparators;

import com.sap.sailing.domain.common.impl.NaturalComparator;
import com.sap.sailing.racecommittee.app.domain.impl.RaceGroupSeriesFleet;

import java.util.Comparator;

public class RegattaSeriesFleetComparator implements Comparator<RaceGroupSeriesFleet> {

    private Comparator<String> nameComparator;

    public RegattaSeriesFleetComparator() {
        nameComparator = new NaturalComparator();
    }

    @Override
    public int compare(RaceGroupSeriesFleet left, RaceGroupSeriesFleet right) {
        int result = nameComparator.compare(left.getRaceGroupName(), right.getRaceGroupName());
        if (result == 0) {
            result = Integer.valueOf(left.getSeriesOrder()).compareTo(right.getSeriesOrder());
            if (result == 0) {
                result = nameComparator.compare(left.getSeriesName(), right.getSeriesName());
                if (result == 0) {
                    result = Integer.valueOf(left.getFleetOrder()).compareTo(right.getFleetOrder());
                    if (result == 0) {
                        result = nameComparator.compare(left.getFleetName(), right.getFleetName());
                    }
                }
            }
        }
        return result;
    }
}
