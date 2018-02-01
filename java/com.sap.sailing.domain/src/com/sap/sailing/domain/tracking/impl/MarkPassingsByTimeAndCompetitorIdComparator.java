package com.sap.sailing.domain.tracking.impl;

import com.sap.sailing.domain.tracking.MarkPassing;

/**
 * Compares two {@link MarkPassing} objects, using their {@link MarkPassing#getTimePoint() time point} as the primary
 * ordering criterion in ascending order. If two mark passings have equal time points, the competitor IDs' string
 * representations are compared as the secondary ordering criterion to ensure that two competitors passing a mark at the
 * same time are still identified as not being equal.
 * 
 * @author Axel Uhl (D043530)
 * 
 */
public class MarkPassingsByTimeAndCompetitorIdComparator extends MarkPassingByTimeComparator {
    private static final long serialVersionUID = 5977491869367296660L;
    public static final MarkPassingsByTimeAndCompetitorIdComparator INSTANCE = new MarkPassingsByTimeAndCompetitorIdComparator();

    @Override
    public int compare(MarkPassing o1, MarkPassing o2) {
        int result = super.compare(o1, o2);
        if (result == 0 && o1 != null) {
            assert o2 != null;
            Object c1ID = o1.getCompetitor().getId();
            Object c2ID = o2.getCompetitor().getId();
            // shortcut comparison and don't use expensive toString() in case IDs are comparable by nature 
            if (c1ID instanceof Comparable<?> && c1ID.getClass() == c2ID.getClass()) {
                @SuppressWarnings("unchecked")
                final Comparable<Object> c1OID = (Comparable<Object>) c1ID;
                result = c1OID.compareTo(o2.getCompetitor().getId());
            } else {
                result = o1.getCompetitor().getId().toString().compareTo(o2.getCompetitor().getId().toString());
            }
        }
        return result;
    }
}
