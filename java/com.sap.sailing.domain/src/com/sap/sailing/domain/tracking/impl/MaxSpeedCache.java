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
 * ) allows clients to specify an interval for which the top speed is to be computed. Therefore, this cache offers a similar
 * API which supports interval-based queries.<p>
 * 
 * The cache listens for GPS fixes being added to the track to which it belongs and takes care of its invalidation.
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
     * {@link #getMaxSpeed(TimePoint, TimePoint)}, and whose {@link Pair#getB() b} component is the maximum speed for
     * that interval. The navigable set is ordered according to ascending <code>to</code> time points, yielding
     * shorter cache intervals before longer intervals.
     */
    private final Map<TimePoint, NavigableSet<Pair<TimePoint, Speed>>> cache;
    
    public MaxSpeedCache(GPSFixTrackImpl<ItemType, FixType> track) {
        this.track = track;
        track.addListener(this);
        cache = new HashMap<TimePoint, NavigableSet<Pair<TimePoint,Speed>>>();
    }

    @Override
    public void gpsFixReceived(FixType fix, ItemType item) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void speedAveragingChanged(long oldMillisecondsOverWhichToAverage, long newMillisecondsOverWhichToAverage) {
        cache.clear();
    }

    public Speed getMaxSpeed(TimePoint from, TimePoint to) {
        Speed result = computeMaxSpeed(from, to);
        cache(from, to, result);
        return result;
    }

    private void cache(TimePoint from, TimePoint to, Speed maxSpeed) {
        NavigableSet<Pair<TimePoint, Speed>> setForFrom = cache.get(from);
        if (setForFrom == null) {
            setForFrom = new ArrayListNavigableSet<Pair<TimePoint, Speed>>(new Comparator<Pair<TimePoint, Speed>>() {
                @Override
                public int compare(Pair<TimePoint, Speed> o1, Pair<TimePoint, Speed> o2) {
                    return o1.getA().compareTo(o2.getA());
                }
            });
            cache.put(from, setForFrom);
        }
        setForFrom.add(new Pair<TimePoint, Speed>(to, maxSpeed));
    }
    
    protected Speed computeMaxSpeed(TimePoint from, TimePoint to) {
        track.lockForRead();
        try {
            // fetch all fixes on this leg so far and determine their maximum speed
            Iterator<FixType> iter = track.getFixesIterator(from, /* inclusive */ true);
            Speed max = Speed.NULL;
            if (iter.hasNext()) {
                while (iter.hasNext()) {
                    FixType fix = iter.next();
                    if (fix.getTimePoint().after(to)) {
                        break;
                    }
                    Speed averagedSpeedAtFixTime = track.getEstimatedSpeed(fix.getTimePoint());
                    if (averagedSpeedAtFixTime.compareTo(max) > 0) {
                        max = averagedSpeedAtFixTime;
                    }
                }
            }
            return max;
        } finally {
            track.unlockAfterRead();
        }
    }
}
