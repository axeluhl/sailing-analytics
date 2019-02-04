package com.sap.sailing.domain.tracking.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.common.tracking.GPSFixMoving;
import com.sap.sailing.domain.tracking.GPSFixTrack;
import com.sap.sse.common.Bearing;
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
        private final LinkedList<Double> courseChangeInDegreesForFixesInWindow; // one shorter than "window"; change[i] is from window[i] to window[i]+1
        // size the total course change list such that resizing should hardly be necessary
        private final List<Double> totalCourseChangeFromBeginningOfWindow;
        private final Duration maximumWindowLength;
        private final double maneuverAngleInDegreesThreshold;
        private Duration windowDuration;
        private double totalCourseChangeInWindowInDegrees;
        
        FixWindow() {
            this.window = new LinkedList<>();
            this.courseChangeInDegreesForFixesInWindow = new LinkedList<>();
            this.windowDuration = Duration.NULL;
            this.totalCourseChangeInWindowInDegrees = 0.0;
            this.maximumWindowLength = boatClass.getApproximateManeuverDuration();
            this.maneuverAngleInDegreesThreshold = boatClass.getManeuverDegreeAngleThreshold();
            this.totalCourseChangeFromBeginningOfWindow = new ArrayList<>(((int) maximumWindowLength.divide(track.getAverageIntervalBetweenRawFixes()))+10);
        }
        
        /**
         * Appends a fix to the end of this fix window. If this produces an interesting candidate within this window, the candidate is returned,
         * and the window is reset in such a way that the same candidate will not be returned a second time.<p>
         * 
         * The window will have established its invariants when this method returns.
         */
        public GPSFixMoving add(GPSFixMoving next) {
            final GPSFixMoving result;
            final GPSFixMoving previous = window.getLast();
            this.window.add(next);
            if (previous != null) {
                final double courseChangeBetweenPreviousAndNextInDegrees = previous.getSpeed().getBearing().getDifferenceTo(next.getSpeed().getBearing()).getDegrees();
                windowDuration = windowDuration.plus(previous.getTimePoint().until(next.getTimePoint()));
                courseChangeInDegreesForFixesInWindow.addLast(courseChangeBetweenPreviousAndNextInDegrees);
                totalCourseChangeFromBeginningOfWindow.add(totalCourseChangeFromBeginningOfWindow.get(totalCourseChangeFromBeginningOfWindow.size()-1)+courseChangeBetweenPreviousAndNextInDegrees);
                totalCourseChangeInWindowInDegrees += courseChangeBetweenPreviousAndNextInDegrees;
                if (windowDuration.compareTo(maximumWindowLength) > 0) {
                    // analysis window has exceeded the typical maneuver duration for the boat class;
                    result = getManeuverCandidate(window, courseChangeInDegreesForFixesInWindow, totalCourseChangeInWindowInDegrees);
                    if (result != null) {
                        // TODO a fix with significant course change was found in the window; reset
                    } else {
                        removeFirst();
                    }
                } else {
                    result = null;
                }
                assert window.size() == courseChangeInDegreesForFixesInWindow.size()+1;
                assert window.size() == totalCourseChangeFromBeginningOfWindow.size()+1;
            } else {
                result = null;
            }
            return result; 
        }

        /**
         * Removes the first fix from the {@link #window} and adjusts all structures to re-establish all invariants.
         * In particular, the {@link #totalCourseChangeInWindow}, the {@link #totalCourseChangeFromBeginningOfWindow}
         * and the {@link #windowDuration}, as well as {@link #courseChangeInDegreesForFixesInWindow} are adjusted.
         */
        private void removeFirst() {
            assert window.size() > 1;
            assert !courseChangeInDegreesForFixesInWindow.isEmpty();
            final GPSFixMoving removed = window.removeFirst();
            final double courseChangeOfFirstInDegrees = courseChangeInDegreesForFixesInWindow.removeFirst();
            totalCourseChangeInWindowInDegrees -= courseChangeOfFirstInDegrees;
            windowDuration = windowDuration.minus(removed.getTimePoint().until(window.getFirst().getTimePoint()));
            // adjust totalCourseChangeFromBeginningOfWindow by subtracting the first course change from all others
            // and shifting all by one position to the "left"
            for (int i=0; i<totalCourseChangeFromBeginningOfWindow.size()-1; i++) {
                // adjust all total course changes by subtracting the 
                totalCourseChangeFromBeginningOfWindow.set(i, totalCourseChangeFromBeginningOfWindow.get(i+1)-courseChangeOfFirstInDegrees);
            }
            totalCourseChangeFromBeginningOfWindow.remove(totalCourseChangeFromBeginningOfWindow.size()-1);
        }

        private GPSFixMoving getManeuverCandidate(LinkedList<GPSFixMoving> window,
                LinkedList<Double> courseChangeInDegreesForFixesInWindow, double totalCourseChangeInWindowInDegrees) {
            // TODO Auto-generated method stub
            return null;
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
        Duration windowDuration = Duration.NULL;
        Bearing totalCogChangeInWindow = Bearing.NORTH;
        track.lockForRead();
        try {
            // TODO try to keep locking intervals as short as possible
            final Iterator<GPSFixMoving> fixIterator = track.getFixesIterator(from, /* inclusive */ true);
            GPSFixMoving next;
            GPSFixMoving previous = null;
            GPSFixMoving fixInWindowWithMaximumCourseChange = null;
            Bearing previousCourseChange = null;
            int indexOfMaximumTotalCourseChange = -1; // -1 means undefined because window is empty
            do {
                if (fixIterator.hasNext()) {
                    next = fixIterator.next();
                } else {
                    next = null;
                }
                window.add(next);
                if (previous != null) {
                    final Bearing courseChangeBetweenPreviousAndNext = previous.getSpeed().getBearing().getDifferenceTo(next.getSpeed().getBearing());
                    windowDuration = windowDuration.plus(previous.getTimePoint().until(next.getTimePoint()));
                    courseChangeInDegreesForFixesInWindow.addLast(courseChangeBetweenPreviousAndNext.getDegrees());
                    totalCogChangeInWindow = totalCogChangeInWindow.add(courseChangeBetweenPreviousAndNext);
                    if (windowDuration.compareTo(maximumWindowLength) > 0) {
                        // analysis window has exceeded the typical maneuver duration for the boat class;
                        final GPSFixMoving maneuverCandidateFix = getManeuverCandidate(window, courseChangeInDegreesForFixesInWindow, totalCogChangeInWindow);
                        if (maneuverCandidateFix != null) {
                            // a fix with significant course change was found in the window;
                            result.add(maneuverCandidateFix);
                            // TODO decide how far to skip; probably start with the fix at or after the maneuver candidate fix...
                        }
                        final GPSFixMoving removed = window.removeFirst();
                        final double courseChangeOfFirst = courseChangeInDegreesForFixesInWindow.removeFirst();
                        windowDuration = windowDuration.minus(removed.getTimePoint().until(window.getFirst().getTimePoint()));
                        // adjust totalCourseChangeFromBeginningOfWindow by subtracting the first course change from all others
                        // and shifting all by one position to the "left"
                        for (int i=0; i<totalCourseChangeFromBeginningOfWindow.size()-1; i++) {
                            // adjust all total course changes by subtracting the 
                            totalCourseChangeFromBeginningOfWindow.set(i, totalCourseChangeFromBeginningOfWindow.get(i+1)-courseChangeOfFirst);
                        }
                        totalCourseChangeFromBeginningOfWindow.remove(totalCourseChangeFromBeginningOfWindow.size()-1);
                    }
                    assert window.size() == courseChangeInDegreesForFixesInWindow.size()+1;
                    previousCourseChange = courseChangeBetweenPreviousAndNext;
                }
                previous = next;
            } while (next != null && !next.getTimePoint().after(to));
        } finally {
            track.unlockAfterRead();
        }
        return result;
    }

}
