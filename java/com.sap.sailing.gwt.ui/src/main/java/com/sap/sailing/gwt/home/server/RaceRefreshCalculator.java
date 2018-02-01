package com.sap.sailing.gwt.home.server;

import com.google.gwt.core.shared.GwtIncompatible;
import com.sap.sailing.gwt.home.communication.race.SimpleRaceMetadataDTO.RaceViewState;
import com.sap.sailing.gwt.home.server.EventActionUtil.RaceCallback;
import com.sap.sse.common.Duration;
import com.sap.sse.common.impl.MillisecondsDurationImpl;
import com.sap.sse.common.impl.MillisecondsTimePoint;

/**
 * {@link RaceCallback} implementation, which calculates a "time to live" for the given {@link RaceContext} based on its
 * {@link RaceContext#getLiveRaceViewState() view state}. The TTL for this {@link RaceRefreshCalculator} instance will
 * potentially be recalculated, when {@link #doForRace(RaceContext)} is called repeatedly. The final {@link #getTTL()
 * TTL} can be used for as refresh interval of "Live races" sections.
 */
@GwtIncompatible
public final class RaceRefreshCalculator implements RaceCallback {
    private long ttl = Duration.ONE_MINUTE.times(3).asMillis();

    @Override
    public void doForRace(RaceContext rc) {
        RaceViewState state = rc.getLiveRaceViewState();
        if(state == RaceViewState.RUNNING) {
            ttl = Math.min(ttl, Duration.ONE_SECOND.times(30).asMillis());
        } else if(state == RaceViewState.SCHEDULED) {
            int timeTillRace = (int) MillisecondsTimePoint.now().until(rc.getStartTime()).asMillis();
            ttl = Math.min(ttl, Math.min(Duration.ONE_MINUTE.asMillis(), timeTillRace));
        } else if(state == RaceViewState.POSTPONED || state == RaceViewState.ABANDONED) {
            ttl = Math.min(ttl, Duration.ONE_MINUTE.asMillis());
        }
    }

    /**
     * Returns the "time to live" calculated for the maybe repeated calls to {@link #doForRace(RaceContext)}.
     * 
     * @return the calculated {@link Duration time to live}
     */
    public Duration getTTL() {
        return new MillisecondsDurationImpl(ttl);
    }
}
