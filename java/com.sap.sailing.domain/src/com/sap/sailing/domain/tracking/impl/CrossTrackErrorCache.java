package com.sap.sailing.domain.tracking.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Logger;

import com.sap.sailing.domain.base.Buoy;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Leg;
import com.sap.sailing.domain.base.Timed;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.base.impl.MeterDistance;
import com.sap.sailing.domain.common.Distance;
import com.sap.sailing.domain.common.LegType;
import com.sap.sailing.domain.common.NoWindException;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.tracking.GPSFix;
import com.sap.sailing.domain.tracking.GPSFixMoving;
import com.sap.sailing.domain.tracking.GPSFixTrack;
import com.sap.sailing.domain.tracking.MarkPassing;
import com.sap.sailing.domain.tracking.RaceChangeListener;
import com.sap.sailing.domain.tracking.Track;
import com.sap.sailing.domain.tracking.TrackedLeg;
import com.sap.sailing.domain.tracking.TrackedRace;

/**
 * (Re-)computing the cross track error for a competitor causes significant amounts of CPU cycles. The cross track error
 * is aggregated for each competitor per race and per leg. The calculation uses the same base data and can be combined.
 * The results can be cached. Cache invalidation becomes necessary as mark passings, buoy positions and boat positions
 * change. For this purpose, this cache subscribes itself as a listener to the {@link TrackedRace} to which it belongs
 * and manages cache invalidations autonomously.
 *   
 * @author Axel Uhl (D043530)
 *
 */
public class CrossTrackErrorCache extends AbstractRaceChangeListener {
    private static final Logger logger = Logger.getLogger(CrossTrackErrorCache.class.getName());
    
    private class CrossTrackErrorSumAndNumberOfFixes implements Timed {
        private static final long serialVersionUID = -278130726836884454L;
        private final TimePoint timePoint;
        private final double distanceInMetersSumFromStart;
        private final int fixCountFromStart;

        public CrossTrackErrorSumAndNumberOfFixes(TimePoint timePoint, double distanceInMetersSumFromStart, int fixCountFromStart) {
            super();
            this.timePoint = timePoint;
            this.distanceInMetersSumFromStart = distanceInMetersSumFromStart;
            this.fixCountFromStart = fixCountFromStart;
        }

        @Override
        public TimePoint getTimePoint() {
            return timePoint;
        }

        public double getDistanceInMetersSumFromStart() {
            return distanceInMetersSumFromStart;
        }

        public int getFixCountFromStart() {
            return fixCountFromStart;
        }
    }
    
    private class CrossTrackErrorSumAndNumberOfFixesTrack extends TrackImpl<CrossTrackErrorSumAndNumberOfFixes> {
        private static final long serialVersionUID = 4884868659665863604L;
        
        public void deleteAllLaterThan(TimePoint from) {
            // TODO use a specialized ArrayList in a specialized ArrayListNavigableSet and then make removeRange public
            lockForWrite();
            try {
                int count = 0;
                Iterator<CrossTrackErrorSumAndNumberOfFixes> i = getRawFixesIterator(from, /* inclusive */ false);
                while (i.hasNext()) {
                    i.next();
                    i.remove();
                    count++;
                }
                if (count > 0) {
                    logger.finest("Deleted "+count+" CrossTrackError cache entries");
                }
            } finally {
                unlockAfterWrite();
            }
        }
        
        public void add(CrossTrackErrorSumAndNumberOfFixes entry) {
            getInternalRawFixes().add(entry);
        }
    }
    
    /**
     * For each competitor for which the {@link #owner owning tracked race} has received GPS fixes, holds the aggregated
     * cross track errors at each (smoothened) fix's time point, as the sum of the cross track distance and the number
     * of fixes considered, starting at or after the competitor's first mark passing's time point. This cache lists
     * these numbers regardless of the leg type. When trying to aggregate the cross track errors for only the
     * {@link LegType#UPWIND upwind legs}, the difference between the aggregates at the mark passings delimiting the
     * upwind legs must be used.<p>
     * 
     * Always up to date for all competitors for which {@link GPSFix}es have been received by the {@link #owner owning
     * tracked race}, based on the notifications sent to this {@link RaceChangeListener}.<p>
     * 
     * TODO check memory consumption; if it's too high, consider reducing the cache to every n seconds so that the (small)
     * increment can be computed quickly and at constant time for any time point. This will be important in case the fix
     * frequency increases
     */
    private final Map<Competitor, CrossTrackErrorSumAndNumberOfFixesTrack> cachePerCompetitor;
    
    private final Map<Competitor, ReentrantReadWriteLock> locksForCompetitors;
    
    private final TrackedRace owner;
    
    public CrossTrackErrorCache(TrackedRace owner) {
        cachePerCompetitor = new HashMap<Competitor, CrossTrackErrorSumAndNumberOfFixesTrack>();
        this.owner = owner;
        this.locksForCompetitors = new HashMap<Competitor, ReentrantReadWriteLock>();
        owner.addListener(this);
    }
    
    /**
     * Answers the query from the cache contents.
     * 
     * @param upwindOnly if <code>true</code>, only fixes in upwind legs are considered during aggregation
     */
    public Distance getAverageCrossTrackError(Competitor competitor, TimePoint from, TimePoint to, boolean upwindOnly) throws NoWindException {
        Track<CrossTrackErrorSumAndNumberOfFixes> cacheForCompetitor = getOrCreateCacheEntryForCompetitor(competitor);
        double distanceInMeters = 0;
        int count = 0;
        owner.getRace().getCourse().lockForRead(); // make sure that course updates don't happen while we're computing
        synchronized (cacheForCompetitor) {
            getOrCreateLockForCompetitor(competitor).readLock().lock();
        }
        try {
            CrossTrackErrorSumAndNumberOfFixes startAggregate = null;
            // iterate leg by leg to support excluding non-upwind legs based on the upwindOnly parameter
            for (Leg leg : owner.getRace().getCourse().getLegs()) {
                final TrackedLeg trackedLeg = owner.getTrackedLeg(leg);
                final MarkPassing legStartMarkPassing = owner.getMarkPassing(competitor, leg.getFrom());
                if (legStartMarkPassing != null) {
                    if (!upwindOnly || trackedLeg.getLegType(legStartMarkPassing.getTimePoint()) == LegType.UPWIND) {
                        TimePoint start;
                        final TimePoint legStart = legStartMarkPassing.getTimePoint();
                        if (legStart.compareTo(from) < 0) {
                            // the interval requested starts after this leg's start:
                            start = from;
                        } else {
                            start = legStart;
                        }
                        final MarkPassing legEndMarkPassing = owner.getMarkPassing(competitor, leg.getTo());
                        if (startAggregate == null) {
                            startAggregate = cacheForCompetitor.getLastFixAtOrBefore(start);
                        }
                        CrossTrackErrorSumAndNumberOfFixes endAggregate;
                        TimePoint end;
                        if (legEndMarkPassing == null || legEndMarkPassing.getTimePoint().compareTo(to) >= 0) {
                            // no next mark passing, or next mark passing is beyond the "to" time point; aggregate up to "to"
                            end = to;
                        } else {
                            end = legEndMarkPassing.getTimePoint();
                        }
                        if (from.compareTo(end) < 0) {
                            endAggregate = cacheForCompetitor.getLastFixAtOrBefore(end);
                            distanceInMeters += endAggregate.getDistanceInMetersSumFromStart()
                                    - startAggregate.getDistanceInMetersSumFromStart();
                            count += endAggregate.getFixCountFromStart() - startAggregate.getFixCountFromStart();
                            startAggregate = endAggregate;
                        }
                    }
                }
            }
        } finally {
            getOrCreateLockForCompetitor(competitor).readLock().unlock();
            owner.getRace().getCourse().unlockAfterRead();
        }
        return count == 0 ? null : new MeterDistance(distanceInMeters / count);
    }
    
    private synchronized ReentrantReadWriteLock getOrCreateLockForCompetitor(Competitor competitor) {
        ReentrantReadWriteLock result = locksForCompetitors.get(competitor);
        if (result == null) {
            result = new ReentrantReadWriteLock();
            locksForCompetitors.put(competitor, result);
        }
        return result;
    }

    /**
     * Updates {@link #cachePerCompetitor} for <code>competitor</code>, starting at <code>from</code>, up to and including
     * the competitor's finish line passing, or the last GPS fix if there is no finish line passing.
     */
    private List<CrossTrackErrorSumAndNumberOfFixes> computeFixesForCacheUpdate(Competitor competitor, TimePoint from) throws NoWindException {
        List<CrossTrackErrorSumAndNumberOfFixes> result = new ArrayList<CrossTrackErrorSumAndNumberOfFixes>();
        final CrossTrackErrorSumAndNumberOfFixesTrack competitorCacheEntry = getOrCreateCacheEntryForCompetitor(competitor);
        synchronized (competitorCacheEntry) {
            getOrCreateLockForCompetitor(competitor).readLock().lock();
        }
        final CrossTrackErrorSumAndNumberOfFixes lastCacheEntryBeforeFrom;
        try {
            lastCacheEntryBeforeFrom = competitorCacheEntry.getLastFixBefore(from);
        } finally {
            getOrCreateLockForCompetitor(competitor).readLock().unlock();
        }
        double distanceInMeters;
        int count;
        if (lastCacheEntryBeforeFrom != null) {
            distanceInMeters = lastCacheEntryBeforeFrom.getDistanceInMetersSumFromStart();
            count = lastCacheEntryBeforeFrom.getFixCountFromStart();
        } else {
            distanceInMeters = 0;
            count = 0;
        }
        GPSFixTrack<Competitor, GPSFixMoving> track = owner.getTrack(competitor);
        GPSFixMoving fix = null;
        owner.getRace().getCourse().lockForRead();
        track.lockForRead();
        try {
            Iterator<GPSFixMoving> fixIter = track.getFixesIterator(from, /* inclusive */true);
            Iterator<Leg> legIter = owner.getRace().getCourse().getLegs().iterator();
            if (legIter.hasNext()) { // if there are no legs, then so there are no tracked legs and therefore no cross-track errors
                Leg currentLeg = legIter.next(); // when set to null this means that no current leg can be found for the current fix and the loop is to abort
                MarkPassing markPassingAtLegStart = owner.getMarkPassing(competitor, currentLeg.getFrom());
                MarkPassing markPassingAtLegEnd = owner.getMarkPassing(competitor, currentLeg.getTo());
                while (currentLeg != null && fixIter.hasNext()) {
                    fix = fixIter.next();
                    // now move to next leg if current leg's end is before or at fix's time point
                    while (currentLeg != null &&
                            markPassingAtLegEnd != null && markPassingAtLegEnd.getTimePoint().compareTo(fix.getTimePoint()) <= 0) {
                        if (legIter.hasNext()) {
                            currentLeg = legIter.next();
                        } else {
                            currentLeg = null;
                        }
                    }
                    // use only fixes that are at or after the current leg's start; this excludes using fixes before the first leg
                    if (currentLeg != null && markPassingAtLegStart != null
                            && fix.getTimePoint().compareTo(markPassingAtLegStart.getTimePoint()) >= 0) {
                        TrackedLeg trackedLeg = owner.getTrackedLeg(currentLeg);
                        Distance xte = owner.getTrackedLeg(trackedLeg.getLeg()).getCrossTrackError(fix.getPosition(),
                                fix.getTimePoint());
                        if (xte != null) {
                            distanceInMeters += xte.getMeters();
                            count++;
                            CrossTrackErrorSumAndNumberOfFixes newCacheEntry = new CrossTrackErrorSumAndNumberOfFixes(fix.getTimePoint(), distanceInMeters, count);
                            result.add(newCacheEntry);
                        }
                    }
                }
            }
        } finally {
            owner.getRace().getCourse().unlockAfterRead();
            track.unlockAfterRead();
        }
        return result;
    }

    private synchronized CrossTrackErrorSumAndNumberOfFixesTrack getOrCreateCacheEntryForCompetitor(Competitor competitor) {
        CrossTrackErrorSumAndNumberOfFixesTrack cacheForCompetitor = cachePerCompetitor.get(competitor);
        if (cacheForCompetitor == null) {
            cacheForCompetitor = new CrossTrackErrorSumAndNumberOfFixesTrack();
            cachePerCompetitor.put(competitor, cacheForCompetitor);
        }
        return cacheForCompetitor;
    }
    
    /**
     * First locks the competitor's cache entry for read access in
     * {@link #computeFixesForCacheUpdate(Competitor, TimePoint)}, then releases the read lock and obtains the write
     * lock. What would otherwise be an area of uncertainty (the time between releasing the read lock and obtaining the
     * write lock) is guarded by a <code>synchronized</code> block, synchronizing on the competitor's cache entry. All
     * other methods trying to obtain a competitor lock must do so in a <code>synchronized</code> block that
     * synchronizes on the competitor's cache entry to make sure they don't cut in between this phase of uncertainty.
     */
    private void invalidate(Competitor competitor, TimePoint from) {
        try {
            CrossTrackErrorSumAndNumberOfFixesTrack competitorCacheEntry = getOrCreateCacheEntryForCompetitor(competitor);
            synchronized (competitorCacheEntry) {
                getOrCreateLockForCompetitor(competitor).readLock().lock();
            }
            List<CrossTrackErrorSumAndNumberOfFixes> update;
            try {
                update = computeFixesForCacheUpdate(competitor, from);
            } catch (Throwable t) {
                getOrCreateLockForCompetitor(competitor).readLock().unlock();
                throw t;
            }
            // synchronized (competitorCacheEntry) {
                // FIXME Using the commented synchronization can cause a deadlock with itself. If one thread holds the read lock, obtains the
                // competitorCacheEntry monitor
                // and then releases the read lock and tries to obtain the write lock, while another thread
                // also holds the read lock, the other thread cannot enter the code to unlock the read lock because
                // it cannot obtain the competitorCacheEntry monitor
                getOrCreateLockForCompetitor(competitor).readLock().unlock();
                getOrCreateLockForCompetitor(competitor).writeLock().lock();
            // }
            try {
                competitorCacheEntry.deleteAllLaterThan(from);
                for (CrossTrackErrorSumAndNumberOfFixes updateFix : update) {
                    competitorCacheEntry.add(updateFix);
                }
            } finally {
                getOrCreateLockForCompetitor(competitor).writeLock().unlock();
            }
        } catch (NoWindException e) {
            logger.severe("Error trying to update cross track error cache: "+e.getMessage());
            logger.throwing(CrossTrackErrorCache.class.getName(), "invalidate", e);
        }
    }

    @Override
    public void competitorPositionChanged(GPSFixMoving fix, Competitor competitor) {
        TimePoint from = owner.getTrack(competitor).getEstimatedPositionTimePeriodAffectedBy(fix).getA();
        invalidate(competitor, from);
    }

    @Override
    public void buoyPositionChanged(GPSFix fix, Buoy buoy) {
        TimePoint from = owner.getOrCreateTrack(buoy).getEstimatedPositionTimePeriodAffectedBy(fix).getA();
        for (Competitor competitor : cachePerCompetitor.keySet()) {
            invalidate(competitor, from);
        }
    }

    @Override
    public void markPassingReceived(Competitor competitor, Map<Waypoint, MarkPassing> oldMarkPassings,
            Iterable<MarkPassing> markPassings) {
        assert oldMarkPassings != null && markPassings != null;
        TimePoint from = null;
        Set<Waypoint> foundOldWaypoints = new HashSet<Waypoint>();
        for (MarkPassing newMarkPassing : markPassings) {
            MarkPassing oldMarkPassing = oldMarkPassings.get(newMarkPassing.getWaypoint());
            if (oldMarkPassing == null) {
                // a new mark passing; invalidate from there:
                from = newMarkPassing.getTimePoint();
                break;
            } else {
                foundOldWaypoints.add(oldMarkPassing.getWaypoint());
                if (!oldMarkPassing.getTimePoint().equals(newMarkPassing.getTimePoint())) {
                    if (oldMarkPassing.getTimePoint().compareTo(newMarkPassing.getTimePoint()) < 0) {
                        from = oldMarkPassing.getTimePoint();
                    } else {
                        from = newMarkPassing.getTimePoint();
                    }
                    break;
                }
            }
        }
        if (foundOldWaypoints.size() != oldMarkPassings.size()) {
            // Some old mark passings were removed; find them and compare their time point to "from."
            // If earlier, set "from" to the earlier time.
            for (Map.Entry<Waypoint, MarkPassing> e : oldMarkPassings.entrySet()) {
                if (!foundOldWaypoints.contains(e.getKey())) {
                    TimePoint timePointOfRemovedMarkPassing = e.getValue().getTimePoint();
                    if (timePointOfRemovedMarkPassing.compareTo(from) < 0) {
                        from = timePointOfRemovedMarkPassing;
                    }
                }
            }
        }
        if (from != null) {
            invalidate(competitor, from);
        }
    }
    
    @Override
    public String toString() {
        return "CrossTrackErrorCache for competitors "+cachePerCompetitor.keySet();
    }
}
