package com.sap.sailing.domain.maneuverdetection.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NavigableSet;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.common.tracking.GPSFixMoving;
import com.sap.sailing.domain.maneuverdetection.IncrementalApproximatedFixesCalculator;
import com.sap.sailing.domain.tracking.GPSFixTrack;
import com.sap.sailing.domain.tracking.MarkPassing;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.tracking.impl.TimedComparator;
import com.sap.sse.common.Duration;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util;
import com.sap.sse.util.impl.ArrayListNavigableSet;

/**
 * Incremental douglas peucker calculator which reuses the already calculated douglas peucker points within legs. It
 * groups the already calculated douglas peucker points by leg. The already calculated douglas peucker points of a leg
 * get only reused, if the leg was completely calculated. The leg will not be reused, if the leg is the first leg within
 * the time range of {@link #approximate(TimePoint, TimePoint)} parameters, and the {@code earliestStart} is different
 * from the earliest {@code earliestStart} which has been ever provided. Analogously, the leg will not be reused, if the
 * leg is the last leg within the queried time range, and the {@code latestEnd} is different from the latest
 * {@code latestEnd} which has been ever provided.
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
public class IncrementalApproximatedFixesCalculatorImpl implements IncrementalApproximatedFixesCalculator {

    private volatile FixesApproximationResult lastFixesApproximationResult = null;
    private final TrackedRace trackedRace;
    private final Competitor competitor;
    private GPSFixTrack<Competitor, GPSFixMoving> track;
    private final Duration minDurationFromLastFixToPreviousMarkPassingToReusePreviousLegFixes;

    public IncrementalApproximatedFixesCalculatorImpl(TrackedRace trackedRace, Competitor competitor) {
        this.trackedRace = trackedRace;
        this.competitor = competitor;
        this.track = trackedRace.getTrack(competitor);
        this.minDurationFromLastFixToPreviousMarkPassingToReusePreviousLegFixes = competitor.getBoat().getBoatClass()
                .getApproximateManeuverDuration().times(3.0);
    }

    @Override
    public Iterable<GPSFixMoving> approximate(TimePoint earliestStart, TimePoint latestEnd) {
        GPSFixMoving latestRawFix = track.getLastRawFix();
        if (latestRawFix == null || !earliestStart.before(latestEnd)) {
            return Collections.emptyList();
        }
        FixesApproximationResult lastFixesApproximationResult = this.lastFixesApproximationResult;
        Iterable<GPSFixMoving> result;
        int alreadyApproximatedLegsCount = lastFixesApproximationResult == null ? 0
                : Util.size(lastFixesApproximationResult.getLegFixesList());
        if (alreadyApproximatedLegsCount < 2) {
            result = approximateInternal(earliestStart, latestEnd);
            storeLastFixesApproximationResult(earliestStart, latestEnd, latestRawFix, result);
        } else {
            List<LegFixes> legFixesListToReuse = new ArrayList<>();
            ListIterator<LegFixes> existingLegFixesIterator = lastFixesApproximationResult.getLegFixesList()
                    .listIterator();
            boolean recalculateFixesAtBeginning;
            if (!earliestStart.equals(lastFixesApproximationResult.getEarliestStart())) {
                // discard fixes of the first leg
                recalculateFixesAtBeginning = true;
                GPSFixMoving earliestFix = track.getFirstFixAtOrAfter(earliestStart);
                int legNumberOfEarliestFix = earliestFix != null ? getLegNumberAt(earliestFix.getTimePoint()) : 0;
                existingLegFixesIterator.next();
                while (existingLegFixesIterator.hasNext()) {
                    LegFixes legFixesToReuse = existingLegFixesIterator.next();
                    if (earliestFix != null && checkIfLegBeginningFarEnoughFromEarliestStartToReuse(earliestFix,
                            legNumberOfEarliestFix, legFixesToReuse)) {
                        existingLegFixesIterator.previous();
                        if (earliestFix.getTimePoint()
                                .equals(legFixesToReuse.getFirstApproximatedFix().getTimePoint())) {
                            recalculateFixesAtBeginning = false;
                        }
                        break;
                    }
                }
            } else {
                recalculateFixesAtBeginning = false;
            }
            boolean recalculateFixesAtEnd;
            if (lastFixesApproximationResult.getLatestEnd().equals(latestEnd) && (latestRawFix.getTimePoint()
                    .equals(lastFixesApproximationResult.getLatestRawFix().getTimePoint()))) {
                recalculateFixesAtEnd = false;
                // reuse existing leg fixes from current iterator cursor position completely
                while (existingLegFixesIterator.hasNext()) {
                    legFixesListToReuse.add(existingLegFixesIterator.next());
                }
            } else {
                recalculateFixesAtEnd = true;
                GPSFixMoving latestFix = track.getLastFixAtOrBefore(latestEnd);
                if (latestEnd != null) {
                    int legNumberOfLatestFix = getLegNumberAt(latestFix.getTimePoint());
                    // cut off last legs and recalculate these legs
                    while (existingLegFixesIterator.hasNext()) {
                        LegFixes legFixesToReuse = existingLegFixesIterator.next();
                        if (checkIfLegEndFarEnoughFromLatestRawFixToReuse(latestFix, legNumberOfLatestFix,
                                legFixesToReuse)) {
                            legFixesListToReuse.add(legFixesToReuse);
                            if (latestFix.getTimePoint()
                                    .equals(legFixesToReuse.getLastApproximatedFix().getTimePoint())) {
                                recalculateFixesAtEnd = false;
                                break;
                            }
                        } else {
                            break;
                        }
                    }
                }
            }

            if (legFixesListToReuse.isEmpty()) {
                result = approximateInternal(earliestStart, latestEnd);
                storeLastFixesApproximationResult(earliestStart, latestEnd, latestRawFix, result);
            } else {
                List<GPSFixMoving> resultList = new ArrayList<>();
                result = resultList;
                if (recalculateFixesAtBeginning) {
                    LegFixes firstLegFixesToReuse = legFixesListToReuse.get(0);
                    Iterable<GPSFixMoving> newApproximatedFixesBefore = approximateInternal(earliestStart,
                            firstLegFixesToReuse.getFirstApproximatedFix().getTimePoint());
                    Iterator<GPSFixMoving> newApproximatedFixesBeforeIterator = newApproximatedFixesBefore.iterator();
                    if (newApproximatedFixesBeforeIterator.hasNext()) {
                        storeNewFixesBeforeExistingFixes(newApproximatedFixesBefore, earliestStart);
                        // add all new fixes to result, but discard the last one, because it is part of the next leg,
                        // which is reused
                        while (newApproximatedFixesBeforeIterator.hasNext()) {
                            GPSFixMoving fix = newApproximatedFixesBeforeIterator.next();
                            if (!fix.getTimePoint()
                                    .equals(firstLegFixesToReuse.getFirstApproximatedFix().getTimePoint())) {
                                resultList.add(fix);
                            } else {
                                break;
                            }
                        }
                    }
                }
                for (LegFixes legFixes : legFixesListToReuse) {
                    Util.addAll(legFixes.getApproximatedFixes(), resultList);
                }
                if (recalculateFixesAtEnd) {
                    LegFixes lastLegFixes = legFixesListToReuse.get(legFixesListToReuse.size() - 1);
                    Iterable<GPSFixMoving> newApproximatedFixesAfter = approximateInternal(
                            lastLegFixes.getLastApproximatedFix().getTimePoint(), latestEnd);
                    Iterator<GPSFixMoving> newApproximatedFixesAfterIterator = newApproximatedFixesAfter.iterator();
                    if (newApproximatedFixesAfterIterator.hasNext()) {
                        storeNewFixesAfterExistingFixes(newApproximatedFixesAfter, latestRawFix, latestEnd);
                        // add all new fixes to result, but discard the first one
                        newApproximatedFixesAfterIterator.next();
                        while (newApproximatedFixesAfterIterator.hasNext()) {
                            GPSFixMoving gpsFixMoving = newApproximatedFixesAfterIterator.next();
                            if (gpsFixMoving.getTimePoint()
                                    .after(lastLegFixes.getLastApproximatedFix().getTimePoint())) {
                                resultList.add(gpsFixMoving);
                            }
                        }
                    }
                }
            }
        }
        return result;
    }

    /**
     * Checks whether the provided {@code legFixesToReuse} is eligible for reuse considering the {@code latestFix} and
     * {@code legNumberOfLatestFix}. The provided {@code legFixesToReuse} is eligible for reuse, if the time point of
     * its last fix matches the time point of {@code latestFix}, or if the time point of the last fix of
     * {@code legFixes} is lying at least {@link #minDurationFromLastFixToPreviousMarkPassingToReusePreviousLegFixes}
     * before the time point of the provided {@code latestFix} and the {@code legNumberOfLatestFix} is higher than the
     * leg number of the leg represented by {@code legFixes}.
     */
    private boolean checkIfLegEndFarEnoughFromLatestRawFixToReuse(GPSFixMoving latestFix, int legNumberOfLatestFix,
            LegFixes legFixesToReuse) {
        GPSFixMoving lastExistingFixOfLeg = legFixesToReuse.getLastApproximatedFix();
        if (latestFix.getTimePoint().equals(lastExistingFixOfLeg.getTimePoint()) || latestFix.getTimePoint().asMillis()
                - lastExistingFixOfLeg.getTimePoint()
                        .asMillis() > minDurationFromLastFixToPreviousMarkPassingToReusePreviousLegFixes.asMillis()
                && legFixesToReuse.getLegNumber() < getLegNumberAt(latestFix.getTimePoint())) {
            return true;
        }
        return false;
    }

    /**
     * Checks whether the provided {@code legFixesToReuse} is eligible for reuse considering the {@code earliestFix} and
     * {@code legNumberOfEarliestFix}. The provided {@code legFixesToReuse} is eligible for reuse, if the time point of
     * its first fix matches the time point of {@code earliestFix}, or if the time point of the first fix of
     * {@code legFixes} is lying at least {@link #minDurationFromLastFixToPreviousMarkPassingToReusePreviousLegFixes}
     * after the time point of the provided {@code earliestFix} and the {@code legNumberOfEarliestFix} is smaller than
     * the leg number of the leg represented by {@code legFixes}.
     */
    private boolean checkIfLegBeginningFarEnoughFromEarliestStartToReuse(GPSFixMoving earliestFix,
            int legNumberOfEarliestFix, LegFixes legFixesToReuse) {
        GPSFixMoving firstExistingFixOfLeg = legFixesToReuse.getFirstApproximatedFix();
        if (earliestFix.getTimePoint().equals(firstExistingFixOfLeg.getTimePoint())
                || firstExistingFixOfLeg.getTimePoint().asMillis() - earliestFix.getTimePoint()
                        .asMillis() > minDurationFromLastFixToPreviousMarkPassingToReusePreviousLegFixes.asMillis()
                        && legFixesToReuse.getLegNumber() > legNumberOfEarliestFix) {
            return true;
        }
        return false;
    }

    /**
     * Stores the fixes retrieved <b>exactly</b> after the last fix of an existing leg within
     * {@link #lastFixesApproximationResult}. For this, the time point of first fix within provided
     * {@code newApproximatedFixesAfter} must lie exactly at the time point of last fix of any existing leg. The new
     * fixes get only stored, if {@link #lastFixesApproximationResult} already exists and the time point of the last fix
     * within {@code newApproximatedFixesAfter} is after the time point of the last fix of the last existing leg.
     * 
     * @param newApproximatedFixesAfter
     *            The new fixes starting from the last fix of an existing leg which was reused
     * @param latestRawFix
     *            The latest raw fix of the track
     * @param latestEnd
     *            The latest start requested within {@link #approximate(TimePoint, TimePoint)}
     */
    private void storeNewFixesAfterExistingFixes(Iterable<GPSFixMoving> newApproximatedFixesAfter,
            GPSFixMoving latestRawFix, TimePoint latestEnd) {
        FixesApproximationResult lastFixesApproximationResult = this.lastFixesApproximationResult;
        if (lastFixesApproximationResult != null) {
            List<LegFixes> existingLegFixesList = lastFixesApproximationResult.getLegFixesList();
            // stored legFixes list cannot be empty (see storeLastFixesApproximationResult())
            LegFixes lastExistingLegFixes = existingLegFixesList.get(existingLegFixesList.size() - 1);
            Iterator<GPSFixMoving> newFixesIterator = newApproximatedFixesAfter.iterator();
            GPSFixMoving lastFix;
            do {
                lastFix = newFixesIterator.next();
            } while (newFixesIterator.hasNext());
            if (lastExistingLegFixes.getLastApproximatedFix().getTimePoint().before(lastFix.getTimePoint())) {
                List<LegFixes> newLegFixesListAfter = groupApproximatedFixesToLegFixes(newApproximatedFixesAfter);
                if (newLegFixesListAfter.size() > 1) {
                    // first fix is always the fix of the previous leg which was reused, when this method is called
                    LegFixes lastExistingLeg = newLegFixesListAfter.remove(0);
                    // just to be sure with the assumption above...
                    if (Util.size(lastExistingLeg.getApproximatedFixes()) == 1) {
                        List<LegFixes> newExistingLegFixesList = new ArrayList<>();
                        boolean lastTimePointOfExistingLegMatched = false;
                        for (LegFixes legFixes : existingLegFixesList) {
                            if (!legFixes.getLastApproximatedFix().getTimePoint()
                                    .equals(lastExistingLeg.getLastApproximatedFix().getTimePoint())) {
                                newExistingLegFixesList.add(legFixes);
                            } else if (!lastTimePointOfExistingLegMatched) {
                                lastTimePointOfExistingLegMatched = true;
                                newExistingLegFixesList.add(legFixes);
                                break;
                            }
                        }
                        // just to be sure with the assumption above...
                        if (lastTimePointOfExistingLegMatched) {
                            newExistingLegFixesList.addAll(newLegFixesListAfter);
                            this.lastFixesApproximationResult = new FixesApproximationResult(
                                    lastFixesApproximationResult.getEarliestStart(), latestEnd, latestRawFix,
                                    newExistingLegFixesList);
                        }
                    }
                }
            }
        }
    }

    /**
     * Stores the fixes retrieved <b>exactly</b> before the beginning fix of an existing leg within
     * {@link #lastFixesApproximationResult}. For this, the time point of last fix within provided
     * {@code newApproximatedFixesBefore} must lie exactly at the time point of beginning fix of any existing leg. The
     * new fixes get only stored, if {@link #lastFixesApproximationResult} already exists and the time point of the
     * first fix within {@code newApproximatedFixesBefore} is before the time point of the first fix of the first
     * existing leg.
     * 
     * @param newApproximatedFixesBefore
     *            The new fixes ending at the first fix of an existing leg which was reused
     * @param earliestStart
     *            The earliest start requested within {@link #approximate(TimePoint, TimePoint)}
     */
    private void storeNewFixesBeforeExistingFixes(Iterable<GPSFixMoving> newApproximatedFixesBefore,
            TimePoint earliestStart) {
        FixesApproximationResult lastFixesApproximationResult = this.lastFixesApproximationResult;
        if (lastFixesApproximationResult != null) {
            List<LegFixes> existingLegFixesList = lastFixesApproximationResult.getLegFixesList();
            // stored legFixes list cannot be empty (see storeLastFixesApproximationResult())
            LegFixes firstExistingLegFixes = existingLegFixesList.get(0);
            GPSFixMoving firstNewFix = newApproximatedFixesBefore.iterator().next();
            if (firstExistingLegFixes.getFirstApproximatedFix().getTimePoint().after(firstNewFix.getTimePoint())) {
                List<LegFixes> newLegFixesListBefore = groupApproximatedFixesToLegFixes(newApproximatedFixesBefore);
                if (newLegFixesListBefore.size() > 1) {
                    // last fix is always the fix at the beginning of the next leg, when this method is called
                    LegFixes firstExistingLeg = newLegFixesListBefore.remove(newLegFixesListBefore.size() - 1);
                    // just to be sure with the assumption above...
                    if (Util.size(firstExistingLeg.getApproximatedFixes()) == 1) {
                        List<LegFixes> newExistingLegFixesList = new ArrayList<>();
                        newExistingLegFixesList.addAll(newLegFixesListBefore);
                        boolean firstTimePointOfExistingLegMatched = false;
                        for (LegFixes legFixes : existingLegFixesList) {
                            if (firstTimePointOfExistingLegMatched) {
                                newExistingLegFixesList.add(legFixes);
                            } else if (legFixes.getFirstApproximatedFix().getTimePoint()
                                    .equals(firstExistingLeg.getFirstApproximatedFix().getTimePoint())) {
                                firstTimePointOfExistingLegMatched = true;
                                newExistingLegFixesList.add(legFixes);
                            }
                        }
                        // just to be sure with the assumption above...
                        if (firstTimePointOfExistingLegMatched) {
                            this.lastFixesApproximationResult = new FixesApproximationResult(earliestStart,
                                    lastFixesApproximationResult.getLatestEnd(),
                                    lastFixesApproximationResult.getLatestRawFix(), newExistingLegFixesList);
                        }
                    }
                }
            }
        }
    }

    private Iterable<GPSFixMoving> approximateInternal(TimePoint earliestStart, TimePoint latestEnd) {
        return trackedRace.approximate(competitor,
                competitor.getBoat().getBoatClass().getMaximumDistanceForCourseApproximation(), earliestStart,
                latestEnd);
    }

    private void storeLastFixesApproximationResult(TimePoint earliestStart, TimePoint latestEnd,
            GPSFixMoving latestRawFix, Iterable<GPSFixMoving> approximatedFixes) {
        FixesApproximationResult lastFixesApproximationResult = this.lastFixesApproximationResult;
        List<LegFixes> legFixesList = groupApproximatedFixesToLegFixes(approximatedFixes);
        if (!legFixesList.isEmpty() && (lastFixesApproximationResult == null
                || legFixesList.size() >= lastFixesApproximationResult.getLegFixesList().size())) {
            this.lastFixesApproximationResult = new FixesApproximationResult(earliestStart, latestEnd, latestRawFix,
                    legFixesList);
        }
    }

    /**
     * Groups provided {@code approximatedFixes} per leg. For this, the time point of the mark passing and the fix is
     * considered. A fix belongs to a leg, if its time point lies at or after the time point of corresponding mark
     * passing, but before the time point of the next mark passing. If a leg does not contain any fixes, it will be
     * skipped. The legs are represented by numbers calculated by {@link #getLegNumberAt(TimePoint)}.
     */
    private List<LegFixes> groupApproximatedFixesToLegFixes(Iterable<GPSFixMoving> approximatedFixes) {
        List<LegFixes> result = new ArrayList<>();
        Iterator<GPSFixMoving> approximatedFixesIterator = approximatedFixes == null ? null
                : approximatedFixes.iterator();
        NavigableSet<MarkPassing> roundings = trackedRace.getMarkPassings(competitor);
        if (approximatedFixesIterator != null && approximatedFixesIterator.hasNext()) {
            if (roundings != null) {
                NavigableSet<MarkPassing> localRoundings = null;
                trackedRace.lockForRead(roundings);
                try {
                    localRoundings = new ArrayListNavigableSet<>(roundings.size(), new TimedComparator());
                    localRoundings.addAll(roundings);
                } finally {
                    trackedRace.unlockAfterRead(roundings);
                }
                int legNumber = 0;
                List<GPSFixMoving> legFixes = new ArrayList<>();
                GPSFixMoving approximatedFix = approximatedFixesIterator.next();
                for (MarkPassing rounding : localRoundings) {
                    do {
                        if (approximatedFix.getTimePoint().before(rounding.getTimePoint())) {
                            legFixes.add(approximatedFix);
                            approximatedFix = approximatedFixesIterator.hasNext() ? approximatedFixesIterator.next()
                                    : null;
                        } else {
                            break;
                        }
                    } while (approximatedFix != null);
                    if (!legFixes.isEmpty()) {
                        result.add(new LegFixes(legNumber, legFixes));
                    }
                    ++legNumber;
                    legFixes = new ArrayList<>();
                    if (!approximatedFixesIterator.hasNext()) {
                        break;
                    }
                }
                while (approximatedFixesIterator.hasNext()) {
                    GPSFixMoving fix = approximatedFixesIterator.next();
                    legFixes.add(fix);
                }
                if (!legFixes.isEmpty()) {
                    result.add(new LegFixes(legNumber, legFixes));
                }
            } else {
                result.add(new LegFixes(0, approximatedFixes));
            }
        }
        return result;
    }

    /**
     * Gets the internal number for a leg sailed by competitor at the provided time point. Number 0 relates to the
     * section, before the start line was crossed. The next higher integer after the number of the last leg refers to
     * the section, after the finish line was passed.
     */
    private int getLegNumberAt(TimePoint timePoint) {
        int legNumber = 0;
        NavigableSet<MarkPassing> roundings = trackedRace.getMarkPassings(competitor);
        if (roundings != null) {
            NavigableSet<MarkPassing> localRoundings = null;
            trackedRace.lockForRead(roundings);
            try {
                localRoundings = new ArrayListNavigableSet<>(roundings.size(), new TimedComparator());
                localRoundings.addAll(roundings);
            } finally {
                trackedRace.unlockAfterRead(roundings);
            }
            for (MarkPassing rounding : localRoundings) {
                if (!rounding.getTimePoint().before(timePoint)) {
                    break;
                }
                ++legNumber;
            }
        }
        return legNumber;
    }

    /**
     * Contains per leg grouped fixes calculated from a douglas peucker calculation. This result is meant to be reused
     * for incremental douglas peucker fixes calculation.
     * 
     * @author Vladislav Chumak (D069712)
     *
     */
    public static class FixesApproximationResult {

        private final TimePoint earliestStart;
        private final TimePoint latestEnd;
        private final GPSFixMoving latestRawFix;
        private final List<LegFixes> legFixesList;

        public FixesApproximationResult(TimePoint earliestStart, TimePoint latestEnd, GPSFixMoving latestRawFix,
                List<LegFixes> legFixesList) {
            this.earliestStart = earliestStart;
            this.latestEnd = latestEnd;
            this.latestRawFix = latestRawFix;
            this.legFixesList = legFixesList;
        }

        public TimePoint getEarliestStart() {
            return earliestStart;
        }

        public TimePoint getLatestEnd() {
            return latestEnd;
        }

        public GPSFixMoving getLatestRawFix() {
            return latestRawFix;
        }

        public List<LegFixes> getLegFixesList() {
            return legFixesList;
        }

    }

    /**
     * Contains fixes which correspond to a certain leg. The corresponding fixes must not be empty. The legs are
     * represented by numbers calculated by {@link #getLegNumberAt(TimePoint)}.
     * 
     * @author Vladislav Chumak (D069712)
     *
     */
    public static class LegFixes {
        private final int legNumber;
        private final Iterable<GPSFixMoving> approximatedFixes;
        private final GPSFixMoving firstApproximatedFix;
        private final GPSFixMoving lastApproximatedFix;

        public LegFixes(int legNumber, Iterable<GPSFixMoving> approximatedFixes) {
            this.legNumber = legNumber;
            this.approximatedFixes = approximatedFixes;
            Iterator<GPSFixMoving> iterator = approximatedFixes.iterator();
            GPSFixMoving lastFix = iterator.next();
            this.firstApproximatedFix = lastFix;
            while (iterator.hasNext()) {
                lastFix = iterator.next();
            }
            this.lastApproximatedFix = lastFix;
        }

        public int getLegNumber() {
            return legNumber;
        }

        public Iterable<GPSFixMoving> getApproximatedFixes() {
            return approximatedFixes;
        }

        public GPSFixMoving getFirstApproximatedFix() {
            return firstApproximatedFix;
        }

        public GPSFixMoving getLastApproximatedFix() {
            return lastApproximatedFix;
        }
    }

}
