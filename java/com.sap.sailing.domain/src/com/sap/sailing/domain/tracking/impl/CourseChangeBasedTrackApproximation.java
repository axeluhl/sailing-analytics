package com.sap.sailing.domain.tracking.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.TreeSet;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.common.SpeedWithBearing;
import com.sap.sailing.domain.common.tracking.GPSFixMoving;
import com.sap.sailing.domain.common.tracking.impl.GPSFixMovingImpl;
import com.sap.sailing.domain.tracking.GPSFixTrack;
import com.sap.sailing.domain.tracking.GPSTrackListener;
import com.sap.sse.common.Duration;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.TimeRange;
import com.sap.sse.common.impl.TimeRangeImpl;

/**
 * Given a {@link GPSFixTrack} containing {@link GPSFixMoving}, an instance of this class finds areas on the track where
 * the course changes sufficiently quickly to indicate a relevant maneuver. The basis for this is the
 * {@link BoatClass}'s {@link BoatClass#getApproximateManeuverDuration() maneuver duration}.
 * <p>
 * 
 * Upon construction, all maneuver candidates for all fixes already contained by the {@link GPSFixTrack} are determined.
 * They can be requested in total or as a subset using {@link #approximate(TimePoint, TimePoint)}.
 * <p>
 * 
 * This object {@link GPSFixTrack#addListener(GPSTrackListener) observes} the {@link GPSFixTrack} for new fixes. If a
 * new fix arrives and it has a time point newer than the last fix in the current analysis window, it is added to the
 * window, updating the maneuver candidates accordingly. If the fix was delivered "out of order" outside of the current
 * analysis window, a temporary {@link FixWindow} is constructed, and a time range around the new fix is re-scanned,
 * again updating the set of maneuver candidates accordingly. If the out-of-order fix fell in between the first and the
 * last fix in the current analysis window, it is inserted into the window accordingly, and the current window is
 * assessed for maneuvers as usual. Being in between fixes, this case does not influence the current analysis window's
 * duration.
 * 
 * @author Axel Uhl (D043530)
 *
 */
public class CourseChangeBasedTrackApproximation implements Serializable, GPSTrackListener<Competitor, GPSFixMoving> {
    private static final long serialVersionUID = -258129016229111573L;
    private final GPSFixTrack<Competitor, GPSFixMoving> track;
    private final BoatClass boatClass;
    private final FixWindow fixWindow;
    private final TreeSet<GPSFixMoving> maneuverCandidates;
    
    /**
     * The fix window consists of the list of fixes, a corresponding list with the course
     * changes at each fix within the window, as well as the aggregated total course change from the beginning of the
     * window up to the respective fix; furthermore, the window duration is maintained to compare against the maximum
     * window length.
     * 
     * @author Axel Uhl (D043530)
     *
     */
    private class FixWindow implements Serializable {
        private static final long serialVersionUID = -6386675214043449110L;

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
        
        public Duration getMaximumWindowLength() {
            return maximumWindowLength;
        }
        
        /**
         * Adds a fix to this fix window, sorted by time point. If this produces an interesting candidate within this window,
         * the candidate is returned, and the window is reset in such a way that the same candidate will not be returned
         * a second time.
         * <p>
         * 
         * The window will have established its invariants when this method returns.
         * 
         * @param next a fix that, in case this window is not empty, is not before the first fix in this window
         * @return a maneuver candidate from the {@link #window} if one became available by adding the {@code next} fix,
         *         or {@code null} if no maneuver candidate became available
         */
        public GPSFixMoving add(GPSFixMoving next) {
            assert window.isEmpty() || !next.getTimePoint().before(window.pollFirst().getTimePoint());
            final GPSFixMoving result;
            final SpeedWithBearing nextSpeed = next.isEstimatedSpeedCached() ? next.getCachedEstimatedSpeed() : track.getEstimatedSpeed(next.getTimePoint());
            if (nextSpeed != null) {
                final ListIterator<GPSFixMoving> listIter = window.listIterator(window.size());
                int insertPosition = window.size();
                GPSFixMoving previous;
                if (window.isEmpty()) {
                    previous = null;
                } else {
                    while ((previous = listIter.previous()).getTimePoint().after(next.getTimePoint())) {
                        insertPosition--;
                    } // previous shall be the last fix in this window before next
                }
                this.window.add(insertPosition, next);
                if (previous != null) {
                    // shortcut estimated COG calculation by checking the fix's cache; if cached, this avoids a
                    // rather expensive ceil/floor search on the track; resort to track.getEstimatedSpeed if not cached
                    final SpeedWithBearing previousSpeed = previous.isEstimatedSpeedCached() ? previous.getCachedEstimatedSpeed() : track.getEstimatedSpeed(previous.getTimePoint());
                    assert previousSpeed != null; // we wouldn't have added the fix in the previous run if it hadn't had a valid speed
                    final double courseChangeBetweenPreviousAndNextInDegrees = previousSpeed.getBearing().getDifferenceTo(nextSpeed.getBearing()).getDegrees();
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
                } else { // the window was empty so far; we added the next fix, but no maneuver can yet be identified in lack of a course change
                    result = null;
                }
            } else {
                // nextSpeed == null; unable to determine COG for next fix, so fix was not added, therefore no new maneuver
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
         * For this, this method selects the fix that has the greatest turn rate from its predecessor towards the
         * direction represented by the signum of the
         * {@link #absoluteMaximumTotalCourseChangeFromBeginningOfWindowInDegrees}. Turn rate is defined as angle change
         * per time.
         * <p>
         * The {@link #window} is left unchanged.
         */
        private GPSFixMoving getManeuverCandidate() {
            final GPSFixMoving result;
            if (absoluteMaximumTotalCourseChangeFromBeginningOfWindowInDegrees >= maneuverAngleInDegreesThreshold) {
                final double signumOfMaximumAbsoluteCourseChange = Math.signum(this.totalCourseChangeFromBeginningOfWindow.get(indexOfMaximumTotalCourseChange));
                double previousTotalCourseChange = 0;
                double maximumAbsoluteCourseChangeInCorrectDirection = -1;
                int indexOfMaximumAbsoluteCourseChangeInCorrectDirection = -1;
                for (int i=0; i<=indexOfMaximumTotalCourseChange; i++) {
                    final double currentTotalCourseChange = totalCourseChangeFromBeginningOfWindow.get(i);
                    final double courseChange = currentTotalCourseChange-previousTotalCourseChange;
                    if (courseChange*signumOfMaximumAbsoluteCourseChange > maximumAbsoluteCourseChangeInCorrectDirection) {
                        maximumAbsoluteCourseChangeInCorrectDirection = courseChange*signumOfMaximumAbsoluteCourseChange;
                        indexOfMaximumAbsoluteCourseChangeInCorrectDirection = i;
                    }
                    previousTotalCourseChange = currentTotalCourseChange;
                }
                result = window.get(indexOfMaximumAbsoluteCourseChangeInCorrectDirection); // pick the fix introducing, not finishing, the highest turn rate
            } else {
                result = null;
            }
            return result;
        }

        /**
         * @return {@code true} if and only if this window is empty or the {@code fix} is not earlier than the first fix
         *         in this window
         */
        public boolean isAtOrAfterFirst(GPSFixMoving fix) {
            return window.isEmpty() || !fix.getTimePoint().before(window.pollFirst().getTimePoint());
        }
    }

    public CourseChangeBasedTrackApproximation(GPSFixTrack<Competitor, GPSFixMoving> track, BoatClass boatClass) {
        super();
        this.track = track;
        this.boatClass = boatClass;
        this.fixWindow = new FixWindow();
        this.maneuverCandidates = new TreeSet<>();
        track.addListener(this);
        addAllFixesOfTrack();
    }
    
    private void addAllFixesOfTrack() {
        track.lockForRead();
        try {
            for (final GPSFixMoving fix : track.getFixes()) {
                addFix(fix);
            }
        } finally {
            track.unlockAfterRead();
        }
    }

    private void addFix(final GPSFixMoving fix) {
        addFix(fix, fixWindow);
    }
    
    /**
     * Adds {@code fix} to the {@code fixWindowToAddTo}, and if a maneuver candidate results, adds
     * that candidate to {@link #maneuverCandidates}.
     */
    private void addFix(final GPSFixMoving fix, FixWindow fixWindowToAddTo) {
        final GPSFixMoving maneuverCandidate = fixWindowToAddTo.add(fix);
        if (maneuverCandidate != null) {
            maneuverCandidates.add(maneuverCandidate);
        }
    }
    
    /**
     * We want the listener relationship with the track to be serialized, e.g., during initial load / replication
     */
    @Override
    public boolean isTransient() {
        return false;
    }

    @Override
    public void gpsFixReceived(GPSFixMoving fix, Competitor competitor, boolean firstFixInTrack) {
        assert competitor == track.getTrackedItem();
        if (fixWindow.isAtOrAfterFirst(fix)) {
            addFix(fix);
        } else {
            final FixWindow outOfOrderWindow = new FixWindow();
            final Duration maximumWindowLength = outOfOrderWindow.getMaximumWindowLength();
            // fix is an out-of-order delivery; construct a new FixWindow and analyze the track around the new fix.
            // A time range around the fix is constructed that will be re-scanned. The time range covers at least
            // maximumWindowLength before and after the fix. If any existing maneuver candidate is found in this
            // time range, the range is extended to cover at least maximumWindowLength before and after that
            // existing candidate, and the candidate is removed for now (in most cases it will be resurrected
            // by the re-scan).
            TimeRange leftPartOfTimeRangeToReScan = new TimeRangeImpl(fix.getTimePoint().minus(maximumWindowLength), fix.getTimePoint());
            TimeRange rightPartOfTimeRangeToReScan = new TimeRangeImpl(fix.getTimePoint(), fix.getTimePoint().plus(maximumWindowLength));
            GPSFixMoving candidateInRange;
            while ((candidateInRange = getExistingManeuverCandidateInRange(leftPartOfTimeRangeToReScan)) != null) {
                maneuverCandidates.remove(candidateInRange);
                leftPartOfTimeRangeToReScan = new TimeRangeImpl(candidateInRange.getTimePoint().minus(maximumWindowLength), candidateInRange.getTimePoint());
            }
            while ((candidateInRange = getExistingManeuverCandidateInRange(rightPartOfTimeRangeToReScan)) != null) {
                maneuverCandidates.remove(candidateInRange);
                rightPartOfTimeRangeToReScan = new TimeRangeImpl(candidateInRange.getTimePoint(), candidateInRange.getTimePoint().plus(maximumWindowLength));
            }
            TimeRange totalRange = new TimeRangeImpl(leftPartOfTimeRangeToReScan.from(), rightPartOfTimeRangeToReScan.to());
            track.lockForRead();
            try {
                for (final GPSFixMoving reAnalysisFix : track.getFixes(totalRange.from(), /* fromInclusive */ true, totalRange.to(), /* toInclusive */ true)) {
                    addFix(reAnalysisFix, outOfOrderWindow);
                }
            } finally {
                track.unlockAfterRead();
            }
        }
    }

    private GPSFixMoving getExistingManeuverCandidateInRange(TimeRange leftPartOfTimeRangeToReScan) {
        final GPSFixMoving firstCandidateAtOrAfterStartOfTimeRange = maneuverCandidates.ceiling(
                /* dummy fix */ createDummyFix(leftPartOfTimeRangeToReScan.from()));
        final GPSFixMoving result;
        if (leftPartOfTimeRangeToReScan.includes(firstCandidateAtOrAfterStartOfTimeRange)) {
            result = firstCandidateAtOrAfterStartOfTimeRange;
        } else {
            result = null;
        }
        return result;
    }

    private GPSFixMovingImpl createDummyFix(TimePoint timePoint) {
        return new GPSFixMovingImpl(/* position */ null, timePoint, /* speed */ null);
    }

    @Override
    public void speedAveragingChanged(long oldMillisecondsOverWhichToAverage, long newMillisecondsOverWhichToAverage) {}

    public Iterable<GPSFixMoving> approximate(TimePoint from, TimePoint to) {
        return maneuverCandidates.subSet(createDummyFix(from), /* fromInclusive */ true, createDummyFix(to), /* toInclusive */ true);
    }

}
