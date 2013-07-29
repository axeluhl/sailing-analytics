package com.sap.sailing.datamining.impl.criterias;

import java.util.Calendar;
import java.util.Collection;

import com.sap.sailing.datamining.DataRetriever;
import com.sap.sailing.datamining.SelectionContext;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.tracking.TrackedRace;

public class YearSelectionCriteria extends AbstractSelectionCriteria<Integer> {

    public YearSelectionCriteria(Collection<Integer> years) {
        super(years);
    }

    @Override
    public boolean matches(SelectionContext context) {
        if (context.getTrackedRace() == null) {
            return false;
        }
        
        for (Integer year : getSelection()) {
            if (raceIsInYear(context.getTrackedRace(), year)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public DataRetriever getDataRetriever(SelectionContext context) {
        // TODO Auto-generated method stub
        return null;
    }

    private boolean raceIsInYear(TrackedRace trackedRace, Integer year) {
        return startOfRaceIsAfter(trackedRace, year) || endOfRaceIsBefore(trackedRace, year);
    }

    private boolean endOfRaceIsBefore(TrackedRace trackedRace, Integer year) {
        if (trackedRace.getEndOfTracking() == null) {
            return false;
        }
        
        return year == getYearOfTimePoint(trackedRace.getEndOfTracking());
    }

    private boolean startOfRaceIsAfter(TrackedRace trackedRace, Integer year) {
        if (trackedRace.getStartOfRace() == null) {
            return false;
        }
        
        return year == getYearOfTimePoint(trackedRace.getStartOfTracking());
    }

    private int getYearOfTimePoint(TimePoint timePoint) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(timePoint.asDate());
        int yearOfTimepoint = calendar.get(Calendar.YEAR);
        return yearOfTimepoint;
    }

}
