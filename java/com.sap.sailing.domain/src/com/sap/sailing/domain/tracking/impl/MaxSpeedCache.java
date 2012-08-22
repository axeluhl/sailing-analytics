package com.sap.sailing.domain.tracking.impl;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NavigableSet;

import com.sap.sailing.domain.common.Speed;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.impl.Util.Pair;
import com.sap.sailing.domain.tracking.GPSFix;
import com.sap.sailing.domain.tracking.GPSFixTrack;
import com.sap.sailing.domain.tracking.GPSTrackListener;
import com.sap.sailing.util.impl.ArrayListNavigableSet;

/**
 * Re-calculating the maximum speed over a {@link GPSFixTrack} is time consuming. When the track grows the way it
 * usually grows (extending at the end), caching can help to reduce the computational effort. The API for computing the
 * maximum speed (
 * {@link GPSFixTrack#getMaximumSpeedOverGround(com.sap.sailing.domain.common.TimePoint, com.sap.sailing.domain.common.TimePoint)}
 * ) allows clients to specify an interval for which the top speed is to be computed. Therefore, this cache offers a
 * similar API which supports interval-based queries.
 * <p>
 * 
 * The cache assumes that queries are usually posed with one of a small set of "from" time points, such as a leg's start
 * or the race starting time. The "to" time points are expected to vary, particularly to grow in case a "live" query is
 * made, or to represent one of a few more or less fixed time points such as a competitor's leg finishing times. With
 * this assumption it seems reasonable to structure the cache such that the "from" time point is a key into a map that
 * stores results for this "from" value. The various results for the same "from" entry are stored in the navigable set
 * with ascending "to" times. When a query comes in, the best fit is determined by looking up the latest "to" that is
 * earlier than or equal to the requested "to." If such an entry is found, the search for a maximum speed can be
 * restricted to the interval between the cache entry's "to" and the "to" time point requested.
 * <p>
 * 
 * When a new GPS fix is recorded for the track for which this is a max-speed cache, invalidation takes place. For this,
 * the cache {@link GPSTrackListener listens} for GPS fixes being added to the track. For the fix added, we'd like to
 * find the time interval within which a
 * {@link GPSFixTrackImpl#getFixesRelevantForSpeedEstimation(TimePoint, NavigableSet)} with a {@link TimePoint} from
 * that interval delivers the fix added in its result. All overlapping cached intervals will then be cropped to the
 * remaining valid interval if their maximum speed time point was in the remaining interval, or removed from the cache
 * otherwise.
 * 
 * @author Axel Uhl (d043530)
 * 
 */
public class MaxSpeedCache<ItemType, FixType extends GPSFix> implements GPSTrackListener<ItemType, FixType> {
    private static final long serialVersionUID = 8825205750854940612L;
    
    private final GPSFixTrackImpl<ItemType, FixType> track;
    
    /**
     * Keys are the "from" time points as passed to {@link #getMaxSpeed(TimePoint, TimePoint)}. Values are navigable
     * sets of pairs whose {@link Pair#getA() a} component is the "to" parameter as passed to
     * {@link #getMaxSpeed(TimePoint, TimePoint)}, and whose {@link Pair#getB() b} component is the track's fix where
     * the averaged maximum speed for that interval was achieved together with the averaged speed at that point. The
     * navigable set is ordered according to ascending <code>to</code> time points, yielding shorter cache intervals
     * before longer intervals.
     */
    private final Map<TimePoint, NavigableSet<Pair<TimePoint, Pair<FixType, Speed>>>> cache;
    
    public MaxSpeedCache(GPSFixTrackImpl<ItemType, FixType> track) {
        this.track = track;
        track.addListener(this);
        cache = new HashMap<TimePoint, NavigableSet<Pair<TimePoint, Pair<FixType, Speed>>>>();
    }

    /**
     * 
     */
    @Override
    public void gpsFixReceived(FixType fix, ItemType item) {
        // TODO find the invalidation interval such that getFixesRelevantForSpeedEstimation, when passed any time point from that interval, produces "fix"
        // TODO find all cache entries that overlap with this interval
        // TODO if the max fix is outside of the invalidation interval, crop cache entry's interval such that it's overlap-free, otherwise remove
    }

    @Override
    public void speedAveragingChanged(long oldMillisecondsOverWhichToAverage, long newMillisecondsOverWhichToAverage) {
        cache.clear();
    }

    public Pair<FixType, Speed> getMaxSpeed(TimePoint from, TimePoint to) {
        Pair<FixType, Speed> result = computeMaxSpeed(from, to);
        cache(from, to, result);
        return result;
    }

    private void cache(TimePoint from, TimePoint to, Pair<FixType, Speed> fixAtMaxSpeed) {
        NavigableSet<Pair<TimePoint, Pair<FixType, Speed>>> setForFrom = cache.get(from);
        if (setForFrom == null) {
            setForFrom = new ArrayListNavigableSet<Pair<TimePoint, Pair<FixType, Speed>>>(new Comparator<Pair<TimePoint, Pair<FixType, Speed>>>() {
                @Override
                public int compare(Pair<TimePoint, Pair<FixType, Speed>> o1, Pair<TimePoint, Pair<FixType, Speed>> o2) {
                    return o1.getA().compareTo(o2.getA());
                }
            });
            cache.put(from, setForFrom);
        }
        setForFrom.add(new Pair<TimePoint, Pair<FixType, Speed>>(to, fixAtMaxSpeed));
    }
    
    protected Pair<FixType, Speed> computeMaxSpeed(TimePoint from, TimePoint to) {
        track.lockForRead();
        try {
            // fetch all fixes on this leg so far and determine their maximum speed
            Iterator<FixType> iter = track.getFixesIterator(from, /* inclusive */ true);
            Speed max = Speed.NULL;
            FixType maxSpeedFix = null;
            if (iter.hasNext()) {
                while (iter.hasNext()) {
                    FixType fix = iter.next();
                    if (fix.getTimePoint().after(to)) {
                        break;
                    }
                    Speed averagedSpeedAtFixTime = track.getEstimatedSpeed(fix.getTimePoint());
                    if (averagedSpeedAtFixTime.compareTo(max) > 0) {
                        max = averagedSpeedAtFixTime;
                        maxSpeedFix = fix;
                    }
                }
            }
            return new Pair<FixType, Speed>(maxSpeedFix, max);
        } finally {
            track.unlockAfterRead();
        }
    }
}
