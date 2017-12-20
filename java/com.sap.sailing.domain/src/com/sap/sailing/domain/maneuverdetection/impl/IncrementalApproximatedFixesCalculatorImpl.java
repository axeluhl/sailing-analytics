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
        long startAt = System.currentTimeMillis();
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
                existingLegFixesIterator.next();
                while (existingLegFixesIterator.hasNext()) {
                    LegFixes legFixesToReuse = existingLegFixesIterator.next();
                    if (earliestFix != null
                            && checkIfLegBeginningFarEnoughFromEarliestStartToReuse(earliestFix, legFixesToReuse)) {
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
                    .equals(lastFixesApproximationResult.getLatestRawFix().getTimePoint())
                    || !latestEnd.after(lastFixesApproximationResult.getLatestRawFix().getTimePoint()))) {
                recalculateFixesAtEnd = false;
                // reuse existing leg fixes from current iterator cursor position completely
                while (existingLegFixesIterator.hasNext()) {
                    legFixesListToReuse.add(existingLegFixesIterator.next());
                }
            } else {
                recalculateFixesAtEnd = true;
                GPSFixMoving latestFix = track.getLastFixAtOrBefore(latestEnd);
                if (latestEnd != null) {
                    // cut off last legs and recalculate these legs
                    while (existingLegFixesIterator.hasNext()) {
                        LegFixes legFixesToReuse = existingLegFixesIterator.next();
                        if (checkIfLegEndFarEnoughFromLatestRawFixToReuse(latestFix, legFixesToReuse)) {
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
                        // add all new fixes to result, but discard the last one
                        while (true) {
                            GPSFixMoving fix = newApproximatedFixesBeforeIterator.next();
                            if (newApproximatedFixesBeforeIterator.hasNext()) {
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
                            resultList.add(newApproximatedFixesAfterIterator.next());
                        }
                    }
                }
            }
        }
        System.out.println((System.currentTimeMillis() - startAt) + " ms for douglas peucker");
        return result;
    }

    private boolean checkIfLegEndFarEnoughFromLatestRawFixToReuse(GPSFixMoving latestFix, LegFixes legFixesToReuse) {
        GPSFixMoving lastExistingFixOfLeg = legFixesToReuse.getLastApproximatedFix();
        if (latestFix.getTimePoint().equals(lastExistingFixOfLeg.getTimePoint()) || latestFix.getTimePoint().asMillis()
                - lastExistingFixOfLeg.getTimePoint()
                        .asMillis() > minDurationFromLastFixToPreviousMarkPassingToReusePreviousLegFixes.asMillis()
                && legFixesToReuse.getLegNumber() < getLegNumberAt(latestFix.getTimePoint())) {
            return true;
        }
        return false;
    }

    private boolean checkIfLegBeginningFarEnoughFromEarliestStartToReuse(GPSFixMoving earliestFix,
            LegFixes legFixesToReuse) {
        GPSFixMoving firstExistingFixOfLeg = legFixesToReuse.getFirstApproximatedFix();
        if (earliestFix.getTimePoint().equals(firstExistingFixOfLeg.getTimePoint())
                || firstExistingFixOfLeg.getTimePoint().asMillis() - earliestFix.getTimePoint()
                        .asMillis() > minDurationFromLastFixToPreviousMarkPassingToReusePreviousLegFixes.asMillis()
                        && legFixesToReuse.getLegNumber() > getLegNumberAt(earliestFix.getTimePoint())) {
            return true;
        }
        return false;
    }

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
                if (!newLegFixesListAfter.isEmpty()) {
                    // first fix is always the fix of the previous leg which was reused, when this method is called
                    newLegFixesListAfter.remove(0);
                    int lastExistingLegNumber = newLegFixesListAfter.remove(0).getLegNumber();
                    List<LegFixes> newExistingLegFixesList = new ArrayList<>();
                    for (LegFixes legFixes : existingLegFixesList) {
                        if (legFixes.getLegNumber() <= lastExistingLegNumber) {
                            newExistingLegFixesList.add(legFixes);
                        } else {
                            break;
                        }
                    }
                    newExistingLegFixesList.addAll(newLegFixesListAfter);
                    this.lastFixesApproximationResult = new FixesApproximationResult(
                            lastFixesApproximationResult.getEarliestStart(), latestEnd, latestRawFix,
                            newExistingLegFixesList);
                }
            }
        }
    }

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
                if (!newLegFixesListBefore.isEmpty()) {
                    // last fix is always the fix at the beginning of the next leg, when this method is called
                    LegFixes firstExistingLeg = newLegFixesListBefore.remove(newLegFixesListBefore.size() - 1);
                    int firstExistingLegNumber = firstExistingLeg.getLegNumber();
                    List<LegFixes> newExistingLegFixesList = new ArrayList<>();
                    newExistingLegFixesList.addAll(newLegFixesListBefore);
                    for (LegFixes legFixes : existingLegFixesList) {
                        if (legFixes.getLegNumber() >= firstExistingLegNumber) {
                            newExistingLegFixesList.add(legFixes);
                        }
                    }
                    this.lastFixesApproximationResult = new FixesApproximationResult(earliestStart,
                            lastFixesApproximationResult.getLatestEnd(), lastFixesApproximationResult.getLatestRawFix(),
                            newExistingLegFixesList);
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
        List<LegFixes> legFixesList = groupApproximatedFixesToLegFixes(approximatedFixes);
        if (!legFixesList.isEmpty()) {
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
