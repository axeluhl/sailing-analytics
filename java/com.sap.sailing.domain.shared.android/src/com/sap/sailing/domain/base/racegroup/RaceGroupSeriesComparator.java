package com.sap.sailing.domain.base.racegroup;

import java.util.Comparator;

/**
 * When current races of a regatta are to be displayed to a user, a reasonable ordering is key to usability. This
 * comparator sorts by their series index (day 1 before day 2; qualification before final, etc.), then by their index
 * within the fleet (R1 before R2, etc.) and lastly by their fleet index (keeping the ordering as set on the server; not
 * to be confused with the ranking ordering set for the fleet; so if on the server silver is placed before gold then
 * that order is kept, regardless of the 1 and 2 values of these fleets). For a typical regatta with qualification,
 * final and medal series, with race columns Q1..Q5, F6..F10, M and fleets Yellow/Blue and Silver/Gold, this would result
 * in an ordering Q1-Yellow, Q1-Blue, ..., Q5-Yellow, Q5-Blue, F6-Silver, F6-Gold, ..., F10-Silver, F10-Gold, Medal.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public class RaceGroupSeriesComparator implements Comparator<RaceGroupSeries> {
    @Override
    public int compare(RaceGroupSeries o1, RaceGroupSeries o2) {
        int result = o1.getRaceGroup().getName().compareTo(o2.getRaceGroup().getName());
        if (result == 0) {
            result = o1.getSeriesOrder() - o2.getSeriesOrder();
            if (result == 0) {
                result = o1.getDisplayName().compareTo(o2.getDisplayName());
            }
        }
        return result;
    }
}
