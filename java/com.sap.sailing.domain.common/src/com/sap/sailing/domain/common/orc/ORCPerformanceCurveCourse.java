package com.sap.sailing.domain.common.orc;

import java.io.Serializable;
import java.util.function.BiFunction;

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
public interface ORCPerformanceCurveCourse extends Serializable {

    /**
     * @return {@link Iterable} object containing all {@link ORCPerformanceCurveLeg}s of an implementing instance of
     *         this class.
     */
    Iterable<ORCPerformanceCurveLeg> getLegs();
    
    /**
     * @return summed up {@link Distance} of all {@link ORCPerformanceCurveLeg}s of an implementing instance of this
     *         class; {@code null} in case the length currently cannot be determined, e.g., because no explicit lengths
     *         have been set and the positions of one or more waypoints are unknown.
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
     *            ...)
     * @param shareOfCurrentLeg
     *            sets the ratio of the lastFinishedLegOneBased + 1 that will be included in the returning object;
     *            values have to be between {@code [0..1]} where 0 means the leg has not been started, and 1 means
     *            it is fully contained in the course.
     * @return new object of an implementing instance of this class which is perhaps a smaller part of the current
     *         object. Same idea as the substring method for Strings.
     */
    ORCPerformanceCurveCourse subcourse(int lastFinishedLegOneBased, double shareOfCurrentLeg);

    /**
     * Like {@link #subcourse(int, double)}, but in case of
     * {@link ORCPerformanceCurveLegTypes#WINDWARD_LEEWARD_REAL_LIVE} the {@code windwardLeewardLegReplacer} function
     * will be used to put a {@link ORCPerformanceCurveLegTypes#TWA} leg ("constructed course") into the resulting
     * course instead, using a leg adapter that is based on the true wind angle on that leg, using the distance as
     * provided by the existing {@link ORCPerformanceCurveLeg} of type
     * {@link ORCPerformanceCurveLegTypes#WINDWARD_LEEWARD_REAL_LIVE WINDWARD_LEEWARD_REAL_LIVE}. All other legs are
     * expected to be returned unchanged.
     * 
     * @param windwardLeewardLegReplacer
     *            if not {@code null}, legs of type {@link ORCPerformanceCurveLegTypes#WINDWARD_LEEWARD_REAL_LIVE} will
     *            be mapped using this function before applying the regular {@link #subcourse(int, double)} logic; if
     *            {@code null}, the method behaves like {@link #subcourse(int, double)}.
     */
    ORCPerformanceCurveCourse subcourse(int zeroBasedIndexOfCurrentLeg, double shareOfCurrentLeg,
            BiFunction<Integer, ORCPerformanceCurveLeg, ORCPerformanceCurveLeg> windwardLeewardLegReplacer);
}
