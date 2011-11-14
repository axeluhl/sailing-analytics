package com.sap.sailing.domain.tracking;

import com.sap.sailing.domain.base.Position;
import com.sap.sailing.domain.base.TimePoint;

public interface WindTrack extends Track<Wind> {
    private final long DEFAULT_MILLISECONDS_OVER_WHICH_TO_AVERAGE_WIND = 30000;

    void add(Wind wind);

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
     */
    Wind getEstimatedWind(Position p, TimePoint at);

    /**
     * A listener is notified whenever a new fix is added to this track
     */
    void addListener(WindListener listener);

    void remove(Wind wind);

    void setMillisecondsOverWhichToAverage(long millisecondsOverWhichToAverage);

    long getMillisecondsOverWhichToAverageWind();
}
