package com.sap.sailing.domain.tracking.impl;

import java.io.Serializable;
import java.util.Comparator;

import com.sap.sailing.domain.common.Wind;
import com.sap.sse.common.Timed;

/**
 * Compares two {@link Wind} objects first by their {@link Wind#getTimePoint() time point}. Only if both
 * wind fixes were taken at the same time, their position is used as a secondary criteria. A more or less
 * arbitrary ordering is used, sorting first by latitude, and if that is equal too, sorting by longitude.
 * 
 * @author Axel Uhl (d043530)
 * 
 */
public class WindComparator implements Comparator<Timed>, Serializable {
    private static final long serialVersionUID = -7202702713168912931L;
    public static final Comparator<Timed> INSTANCE = new WindComparator();

    @Override
    public int compare(Timed o1, Timed o2) {
        int result = o1 == null ? o2 == null ? 0 : -1 : o2 == null ? 1 : o1.getTimePoint().compareTo(o2.getTimePoint());
        if (result == 0) {
            if (o1 instanceof Wind && o2 instanceof Wind) {
                // try to decide based on position; note that position may be null
                Wind o1Wind = (Wind) o1;
                Wind o2Wind = (Wind) o2;
                if (o1Wind.getPosition() == null) {
                    if (o2Wind.getPosition() != null) {
                        result = -1;
                    } // else both are null and 0 is already the correct answer
                } else {
                    if (o2Wind.getPosition() == null) {
                        result = 1;
                    } else {
                        // use the coordinates as secondary criteria:
                        result = Double.compare(o1Wind.getPosition().getLatDeg(), o2Wind.getPosition().getLatDeg());
                        if (result == 0) {
                            result = Double.compare(o1Wind.getPosition().getLngDeg(), o2Wind.getPosition().getLngDeg());
                        }
                    }
                }
            }
        }
        return result;
    }
}

