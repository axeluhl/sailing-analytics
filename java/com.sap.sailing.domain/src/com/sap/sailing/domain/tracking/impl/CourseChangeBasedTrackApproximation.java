package com.sap.sailing.domain.tracking.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.common.tracking.GPSFixMoving;
import com.sap.sailing.domain.tracking.GPSFixTrack;
import com.sap.sse.common.Duration;
import com.sap.sse.common.TimePoint;

/**
 * Given a {@link GPSFixTrack} containing {@link GPSFixMoving}, an instance of this class finds areas on the track where
 * the course changes sufficiently quickly to indicate a relevant maneuver. The basis for this is the
 * {@link BoatClass}'s {@link BoatClass#getApproximateManeuverDuration() maneuver duration}.
 * 
 * TODO future versions of this class shall be stateful, keeping track of the interesting points identified so
 * far and only adjusting incrementally based on a listener pattern.
 * 
 * @author Axel Uhl (D043530)
 *
 */
public class CourseChangeBasedTrackApproximation {
    private final GPSFixTrack<Competitor, GPSFixMoving> track;
    private final BoatClass boatClass;
    
    /**
     * The fix window consists of the list of fixes, a corresponding list with the course
     * changes at each fix within the window, as well as the aggregated total course change from the beginning of the
     * window up to the respective fix; furthermore, the window duration is maintained to compare against the maximum
     * window length.
     * 
     * @author Axel Uhl (D043530)
     *
     */
    private class FixWindow {
        private final LinkedList<GPSFixMoving> window;
        
        /**
         * one shorter than "window"; change[i] is from window[i] to window[i]+1
         */
        private final List<Double> totalCourseChangeFromBeginningOfWindow;

        private final Duration maximumWindowLength;
        private final double maneuverAngleInDegreesThreshold;
        private Duration windowDuration;
        
        /**
         * The absolute of the value found at index {@link #indexOfMaximumTotalCourseChange} in
         * {@link #totalCourseChangeFromBeginningOfWindow}.
         */
        private double absoluteMaximumTotalCourseChangeFromBeginningOfWindowInDegrees;
        
        /**
         * -1 means undefined because window is empty; otherwise an index into
         * {@link #totalCourseChangeFromBeginningOfWindow} such that the absolute
         * value at that index is maximal.
         */
        private int indexOfMaximumTotalCourseChange; 

        FixWindow() {
            this.window = new LinkedList<>();
            this.absoluteMaximumTotalCourseChangeFromBeginningOfWindowInDegrees = 0;
            this.indexOfMaximumTotalCourseChange = -1;
            this.windowDuration = Duration.NULL;
            // use twice the maneuver duration to also catch slowly-executed gybes
            this.maximumWindowLength = boatClass.getApproximateManeuverDuration().times(2);
            this.maneuverAngleInDegreesThreshold = boatClass.getManeuverDegreeAngleThreshold();
            this.totalCourseChangeFromBeginningOfWindow = new ArrayList<>(((int) maximumWindowLength.divide(track.getAverageIntervalBetweenRawFixes()))+10);
        }
        
        /**
         * Appends a fix to the end of this fix window. If this produces an interesting candidate within this window,
         * the candidate is returned, and the window is reset in such a way that the same candidate will not be returned
         * a second time.
         * <p>
         * 
         * The window will have established its invariants when this method returns.
         * 
         * @return a maneuver candidate from the {@link #window} if one became available by adding the {@code next} fix,
         *         or {@code null} if no maneuver candidate became available
         */
        public GPSFixMoving add(GPSFixMoving next) {
            final GPSFixMoving result;
            final GPSFixMoving previous = window.peekLast();
            this.window.add(next);
            if (previous != null) {
                final double courseChangeBetweenPreviousAndNextInDegrees = previous.getSpeed().getBearing().getDifferenceTo(next.getSpeed().getBearing()).getDegrees();
                windowDuration = windowDuration.plus(previous.getTimePoint().until(next.getTimePoint()));
                if (totalCourseChangeFromBeginningOfWindow.isEmpty()) {
                    totalCourseChangeFromBeginningOfWindow.add(courseChangeBetweenPreviousAndNextInDegrees);
                    absoluteMaximumTotalCourseChangeFromBeginningOfWindowInDegrees = Math.abs(courseChangeBetweenPreviousAndNextInDegrees);
                    indexOfMaximumTotalCourseChange = 0;
                } else {
                    final double totalCourseChangeFromBeginningOfWindowForCurrentFix = totalCourseChangeFromBeginningOfWindow.get(totalCourseChangeFromBeginningOfWindow.size()-1)
                            + courseChangeBetweenPreviousAndNextInDegrees;
                    totalCourseChangeFromBeginningOfWindow.add(totalCourseChangeFromBeginningOfWindowForCurrentFix);
                    if (Math.abs(totalCourseChangeFromBeginningOfWindowForCurrentFix) > absoluteMaximumTotalCourseChangeFromBeginningOfWindowInDegrees) {
                        absoluteMaximumTotalCourseChangeFromBeginningOfWindowInDegrees = Math.abs(totalCourseChangeFromBeginningOfWindowForCurrentFix);
                        indexOfMaximumTotalCourseChange = totalCourseChangeFromBeginningOfWindow.size()-1;
                    }
                }
                if (windowDuration.compareTo(maximumWindowLength) > 0) {
                    result = tryToExtractManeuverCandidate();
                } else {
                    result = null;
                }
                assert window.isEmpty() && totalCourseChangeFromBeginningOfWindow.isEmpty() || window.size() == totalCourseChangeFromBeginningOfWindow.size()+1;
            } else {
                result = null;
            }
            return result; 
        }

        /**
         * Tries to extract a maneuver candidate from the current {@link #window}. See {@link #getManeuverCandidate()}.
         * Basically, the maximum course change in the window has to be equal to or exceed the maneuver threshold. If
         * such a candidate is found, all fixes before the candidate as well as the candidate itself are
         * {@link #removeFirst() removed} from the {@link #window}, and all invariants are re-established.<p>
         * 
         * Usually, this method will be called by {@link #add(GPSFixMoving)}, but especially after having added the last
         * fix of a set of fixes the client may want to know if the current window already contains a maneuver candidate,
         * regardless of whether the typical maneuver duration has been exceeded by the window contents. In such a case,
         * clients may want to call this method which will return a maneuver candidate if available and clean up the
         * window accordingly.
         * 
         * @return the maneuver candidate if one was found in the {@link #window}, or {@code null} if no such candidate
         *         was found.
         */
        public GPSFixMoving tryToExtractManeuverCandidate() {
            final GPSFixMoving result;
            // analysis window has exceeded the typical maneuver duration for the boat class;
            result = getManeuverCandidate();
            if (result != null) {
                while (!removeFirst().equals(result)); // remove all including the maneuver fix
            } else {
                removeFirst();
            }
            return result;
        }

        /**
         * Removes the first fix from the {@link #window} and adjusts all structures to re-establish all invariants. In
         * particular, the {@link #totalCourseChangeInWindow}, the {@link #totalCourseChangeFromBeginningOfWindow} and
         * the {@link #windowDuration}, as well as {@link #courseChangeInDegreesForFixesInWindow} are adjusted.
         * 
         * @return the fix removed from the beginning of this window
         */
        private GPSFixMoving removeFirst() {
            assert !window.isEmpty();
            final GPSFixMoving removed = window.removeFirst();
            windowDuration = window.isEmpty() ? Duration.NULL : windowDuration.minus(removed.getTimePoint().until(window.getFirst().getTimePoint()));
            // adjust totalCourseChangeFromBeginningOfWindow by subtracting the first course change from all others
            // and shifting all by one position to the "left"
            absoluteMaximumTotalCourseChangeFromBeginningOfWindowInDegrees = 0;
            if (totalCourseChangeFromBeginningOfWindow.size() <= 1) { // no more than one element left; can't tell any course change
                indexOfMaximumTotalCourseChange = -1;
            } else {
                final double courseChangeOfFirstInDegrees = totalCourseChangeFromBeginningOfWindow.get(0);
                for (int i=0; i<totalCourseChangeFromBeginningOfWindow.size()-1; i++) {
                    // adjust all total course changes by subtracting the 
                    final double totalCourseChangeFromBeginningOfWindowForFixAtIndex = totalCourseChangeFromBeginningOfWindow.get(i+1)-courseChangeOfFirstInDegrees;
                    if (Math.abs(totalCourseChangeFromBeginningOfWindowForFixAtIndex) > absoluteMaximumTotalCourseChangeFromBeginningOfWindowInDegrees) {
                        absoluteMaximumTotalCourseChangeFromBeginningOfWindowInDegrees = Math.abs(totalCourseChangeFromBeginningOfWindowForFixAtIndex);
                        indexOfMaximumTotalCourseChange = i;
                    }
                    totalCourseChangeFromBeginningOfWindow.set(i, totalCourseChangeFromBeginningOfWindowForFixAtIndex);
                }
            }
            if (!totalCourseChangeFromBeginningOfWindow.isEmpty()) { // only try to remove if not removing last element of window
                totalCourseChangeFromBeginningOfWindow.remove(totalCourseChangeFromBeginningOfWindow.size()-1);
            } else {
                assert window.isEmpty();
            }
            return removed;
        }

        /**
         * If the {@link #absoluteMaximumTotalCourseChangeFromBeginningOfWindowInDegrees} is greater than or equal to
         * the {@link #maneuverAngleInDegreesThreshold}, the fix representing the maneuver candidate best is returned.
         * The {@link #window} is left unchanged.
         */
        private GPSFixMoving getManeuverCandidate() {
            final GPSFixMoving result;
            if (absoluteMaximumTotalCourseChangeFromBeginningOfWindowInDegrees >= maneuverAngleInDegreesThreshold) {
                result = window.get(indexOfMaximumTotalCourseChange+1); // the index at i is for window[i]..window[i+1], so return window[i+1]
            } else {
                result = null;
            }
            return result;
        }
    }

    public CourseChangeBasedTrackApproximation(GPSFixTrack<Competitor, GPSFixMoving> track, BoatClass boatClass) {
        super();
        this.track = track;
        this.boatClass = boatClass;
    }
    
    public Iterable<GPSFixMoving> approximate(TimePoint from, TimePoint to) {
        final List<GPSFixMoving> result = new ArrayList<>();
        final FixWindow window = new FixWindow();
        track.lockForRead();
        try {
            // TODO try to keep locking intervals as short as possible
            final Iterator<GPSFixMoving> fixIterator = track.getFixesIterator(from, /* inclusive */ true);
            GPSFixMoving next;
            do {
                if (fixIterator.hasNext()) {
                    next = fixIterator.next();
                    final GPSFixMoving maneuverCandidate = window.add(next);
                    if (maneuverCandidate != null) {
                        result.add(maneuverCandidate);
                    }
                } else {
                    next = null;
                }
            } while (next != null && !next.getTimePoint().after(to));
        } finally {
            track.unlockAfterRead();
        }
        final GPSFixMoving lastManeuverCandidate = window.tryToExtractManeuverCandidate();
        if (lastManeuverCandidate != null) {
            result.add(lastManeuverCandidate);
        }
        return result;
    }

}
