package com.sap.sailing.domain.tracking.impl;

import java.io.Serializable;
import java.util.Comparator;

import com.sap.sailing.domain.tracking.MarkPassing;

/**
 * Compares two {@link MarkPassing} objects, using their {@link MarkPassing#getTimePoint() time point} as the only
 * ordering criterion in ascending order. If two mark passings have equal time points, they are considered equal,
 * regardless the competitor. Don't use this comparator for collections in which mark passings for multiple competitors
 * may be managed and shall be treated as different. Use {@link MarkPassingsByTimeAndCompetitorIdComparator} instead.
 * 
 * @author Axel Uhl (D043530)
 * 
 */
public class MarkPassingByTimeComparator implements Comparator<MarkPassing>, Serializable {
    private static final long serialVersionUID = 5758820139503482888L;
    public static final MarkPassingByTimeComparator INSTANCE = new MarkPassingByTimeComparator();
    
    @Override
    public int compare(MarkPassing o1, MarkPassing o2) {
        return o1 == null ?
                o2 == null ? 0 : -1
              : o2 == null ? 1 : o1.getTimePoint() == null ?
                      (o2.getTimePoint() == null ? 0 : -1)
                    :  o2.getTimePoint() == null ? 1 : o1.getTimePoint().compareTo(o2.getTimePoint());
    }
}
