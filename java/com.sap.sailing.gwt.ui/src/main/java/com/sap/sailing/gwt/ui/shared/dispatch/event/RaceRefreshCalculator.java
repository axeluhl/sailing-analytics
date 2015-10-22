package com.sap.sailing.gwt.ui.shared.dispatch.event;

import com.google.gwt.core.shared.GwtIncompatible;
import com.sap.sailing.gwt.ui.shared.dispatch.event.EventActionUtil.RaceCallback;
import com.sap.sailing.gwt.ui.shared.race.SimpleRaceMetadataDTO.RaceViewState;
import com.sap.sse.common.Duration;
import com.sap.sse.common.impl.MillisecondsDurationImpl;
import com.sap.sse.common.impl.MillisecondsTimePoint;

@GwtIncompatible
final class RaceRefreshCalculator implements RaceCallback {
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

    public Duration getTTL() {
        return new MillisecondsDurationImpl(ttl);
    }
}
