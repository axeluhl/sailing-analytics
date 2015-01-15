package com.sap.sailing.domain.tracking;

import com.sap.sailing.domain.common.Distance;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.impl.MeterDistance;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util;

public interface WindTrack extends DynamicTrack<Wind> {
    static final long DEFAULT_MILLISECONDS_OVER_WHICH_TO_AVERAGE_WIND = 30000;
    
    public static final Distance WIND_HALF_CONFIDENCE_DISTANCE = new MeterDistance(100);
    public static final long WIND_HALF_CONFIDENCE_TIME_MILLIS = 10000l;

    /**
     * Estimates a wind force and direction based on tracked wind data.<p>
     * 
     * An implementation will typically put some averaging algorithm in place to avoid
     * "jumpy" measurements, making leaderboard and advantage line computations too edgy.
     * If a time-based averaging mechanism is used and <code>at</code> is more than the
     * averaging interval after the last known measurement then at least the last
     * measurement before <code>at</code> will be used to avoid ending up with no
     * estimate at all.<p>
     * 
     * If the track has no wind data at all, <code>null</code> will be returned.
     * Attention: The TimePoint of the returned Wind is NOT necessarily equal to the requested timepoint.  
     */
    Wind getAveragedWind(Position p, TimePoint at);
    
    WindWithConfidence<Util.Pair<Position, TimePoint>> getAveragedWindWithConfidence(Position p, TimePoint at);

    /**
     * A listener is notified whenever a new fix is added to this track
     */
    void addListener(WindListener listener);

    void remove(Wind wind);

    void setMillisecondsOverWhichToAverage(long millisecondsOverWhichToAverage);

    long getMillisecondsOverWhichToAverageWind();
}
