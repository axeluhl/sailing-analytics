package com.sap.sailing.domain.orc;

import com.sap.sse.common.Distance;

/**
 * Represents a simplified version of a {@link Course} for usage in the {@link ORCPerformanceCurve} calculation.
 * The course consists out of an array of {@link ORCPerformanceCurveLeg}s, where each is reduced to the TWA and
 * length information.
 * An {@link ORCPerformanceCurveCourse} can also represent the sailed subcourse up to an timepoint of an competitor
 * and isn't limited to the representation of a full race course.
 * 
 * @author Daniel Lisunkin (i505543)
 *
 */
public interface ORCPerformanceCurveCourse {

    /**
     * @return {@link Iterable} object containing all {@link ORCPerformanceCurveLeg}s of an implementing instance of
     *         this class.
     */
    Iterable<ORCPerformanceCurveLeg> getLegs();
    
    /**
     * @return summed up {@link Distance} of all {@link ORCPerformanceCurveLeg}s of an implementing instance of this
     *         class
     */
    default Distance getTotalLength() {
        Distance result = Distance.NULL;
        for (ORCPerformanceCurveLeg leg : getLegs()) {
            result = result.add(leg.getLength());
        }
        return result;
    }
    
    /**
     * @param lastFinishedLegOneBased
     *            equals the index of the last fully completed leg. (e.g. 0 for no finished legs, 1 for 1 finished leg,
     *            ...
     * @param perCentOfCurrentLeg
     *            sets the percentage of the lastFinishedLegOneBased + 1 that will be included in the returning object
     * @return new object of an implementing instance of this class which is perhaps a smaller part of the current
     *         object. Same idea as the substring method for Strings.
     */
    ORCPerformanceCurveCourse subcourse(int lastFinishedLegOneBased, double perCentOfCurrentLeg);
}
