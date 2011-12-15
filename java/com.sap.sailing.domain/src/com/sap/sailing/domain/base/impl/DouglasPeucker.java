package com.sap.sailing.domain.base.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import com.sap.sailing.domain.base.Bearing;
import com.sap.sailing.domain.base.Distance;
import com.sap.sailing.domain.base.Position;
import com.sap.sailing.domain.base.TimePoint;
import com.sap.sailing.domain.tracking.GPSFix;
import com.sap.sailing.domain.tracking.GPSFixTrack;
import com.sap.sailing.util.Util.Pair;

/**
 * Implements the (Ramer)-Douglas-Peucker algorithm on a segment of a {@link GPSFixTrack} with a configurable distance
 * threshold.
 * 
 * @author Axel Uhl (d043530)
 * 
 */
public class DouglasPeucker<ItemType, FixType extends GPSFix> {
    private final GPSFixTrack<ItemType, FixType> track;

    public DouglasPeucker(GPSFixTrack<ItemType, FixType> track) {
        this.track = track;
    }

    private Pair<GPSFix, Distance> getFixWithGreatestCrossTrackErrorInInterval(TimePoint from, TimePoint to) {
        Distance maxDistance = Distance.NULL;
        GPSFix fixFurthestAway = null;
        FixType firstFixAtOrAfter = track.getFirstFixAtOrAfter(from);
        Pair<GPSFix, Distance> result = null;
        if (firstFixAtOrAfter != null) {
            Position fromPosition = firstFixAtOrAfter.getPosition();
            FixType toFix = track.getLastFixAtOrBefore(to);
            Bearing bearing = fromPosition.getBearingGreatCircle(toFix.getPosition());
            synchronized (track) {
                Iterator<FixType> fixIter = track.getFixesIterator(from, /* inclusive */false);
                while (fixIter.hasNext()) {
                    FixType fix = fixIter.next();
                    if (fix.getTimePoint().compareTo(to) > 0) {
                        break;
                    }
                    Distance crossTrackError = fix.getPosition().crossTrackError(fromPosition, bearing);
                    if (crossTrackError.compareTo(maxDistance) > 0) {
                        maxDistance = crossTrackError;
                        fixFurthestAway = fix;
                    }
                }
            }
            result = new Pair<GPSFix, Distance>(fixFurthestAway, maxDistance);
        }
        return result;
    }
    
    /**
     * @param maxDistance
     *            maximum error of the approximation; all fixes or the track are closer than this distance to the fix
     *            sequence returned
     * @return a sequence of fixes from the {@link #track} starting <em>after</em> time point <code>from</code> and
     *         including the last fix at or before time <code>to</code> that approximates the {@link #track} such that
     *         the maximum distance of any fix on the {@link #track} to the approximation is less than
     *         <code>maxDistance</code>. Always returns a non-<code>null</code> list which may, however, be empty.
     *         Note, that if the fixes contain bearing information, the bearing is not the bearing leading to the
     *         next fix of the approximation but the bearing the tracked item had at the point in time of the
     *         approximation fix.
     */
    public List<FixType> approximate(Distance maxDistance, TimePoint from, TimePoint to) {
        List<FixType> resultWithoutFirstFix = approximateWithoutFirst(maxDistance, from, to);
        List<FixType> result = new ArrayList<FixType>(resultWithoutFirstFix.size() + 1);
        FixType firstFixAtOrAfter = track.getFirstFixAtOrAfter(from);
        if (firstFixAtOrAfter != null) {
            result.add(firstFixAtOrAfter);
        }
        for (FixType f : resultWithoutFirstFix) {
            result.add(f);
        }
        return result;
    }
    
    /**
     * @return a non-<code>null</code> list which may be empty
     */
    private List<FixType> approximateWithoutFirst(Distance maxDistance, TimePoint from, TimePoint to) {
        List<FixType> result;
        Pair<GPSFix, Distance> fixAndDistance = getFixWithGreatestCrossTrackErrorInInterval(from, to);
        if (fixAndDistance == null || fixAndDistance.getB().compareTo(maxDistance) < 0) {
            // reached desired accuracy for interval from..to
            FixType lastFixAtOrBefore = track.getLastFixAtOrBefore(to);
            if (lastFixAtOrBefore == null || lastFixAtOrBefore.getTimePoint().compareTo(from) < 0) {
                result = Collections.emptyList();
            } else {
                result = Collections.singletonList(lastFixAtOrBefore);
            }
        } else {
            List<FixType> left = approximateWithoutFirst(maxDistance, from, fixAndDistance.getA().getTimePoint());
            List<FixType> right = approximateWithoutFirst(maxDistance, fixAndDistance.getA().getTimePoint(), to);
            result = new ArrayList<FixType>(left.size()+right.size());
            for (FixType fixFromLeft : left) {
                result.add(fixFromLeft);
            }
            for (FixType fixFromRight : right) {
                result.add(fixFromRight);
            }
        }
        return result;
    }
}
