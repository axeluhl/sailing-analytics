package com.sap.sailing.domain.tracking.impl;

import java.util.Iterator;

import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.Speed;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.tracking.GPSFix;
import com.sap.sailing.domain.tracking.GPSFixTrack;
import com.sap.sailing.domain.tracking.GPSTrackListener;

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
    
    public MaxSpeedCache(GPSFixTrackImpl<ItemType, FixType> track) {
        this.track = track;
        track.addListener(this);
    }

    @Override
    public void gpsFixReceived(FixType fix, ItemType item) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void speedAveragingChanged(long oldMillisecondsOverWhichToAverage, long newMillisecondsOverWhichToAverage) {
        // TODO Auto-generated method stub
        
    }

    public Speed getMaxSpeed(TimePoint from, TimePoint to) {
        track.lockForRead();
        try {
            // TODO implement a smart cache for max SOG
            // fetch all fixes on this leg so far and determine their maximum speed
            Iterator<FixType> iter = track.getFixesIterator(from, /* inclusive */ true);
            Speed max = Speed.NULL;
            if (iter.hasNext()) {
                Position lastPos = track.getEstimatedPosition(from, false);
                while (iter.hasNext()) {
                    FixType fix = iter.next();
                    Speed fixSpeed = track.getSpeed(fix, lastPos, from);
                    if (fixSpeed.compareTo(max) > 0) {
                        max = fixSpeed;
                    }
                    lastPos = fix.getPosition();
                    from = fix.getTimePoint();
                }
            }
            return max;
        } finally {
            track.unlockAfterRead();
        }
    }
}
