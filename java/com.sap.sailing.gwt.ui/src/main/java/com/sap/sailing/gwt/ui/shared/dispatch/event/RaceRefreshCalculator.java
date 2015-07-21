package com.sap.sailing.gwt.ui.shared.dispatch.event;

import com.google.gwt.core.shared.GwtIncompatible;
import com.sap.sailing.gwt.ui.shared.dispatch.event.EventActionUtil.RaceCallback;
import com.sap.sailing.gwt.ui.shared.race.SimpleRaceMetadataDTO.RaceViewState;
import com.sap.sse.common.impl.MillisecondsTimePoint;

@GwtIncompatible
final class RaceRefreshCalculator implements RaceCallback {
    private long ttl = 1000 * 60 * 3;

    @Override
    public void doForRace(RaceContext rc) {
        RaceViewState state = rc.getLiveRaceViewState();
        if(state == RaceViewState.RUNNING) {
            ttl = Math.min(ttl, 1000 * 30);
        } else if(state == RaceViewState.SCHEDULED) {
            int timeTillRace = (int) MillisecondsTimePoint.now().until(rc.getStartTime()).asMillis();
            ttl = Math.min(ttl, Math.min(1000 * 60, timeTillRace));
        } else if(state == RaceViewState.POSTPONED || state == RaceViewState.ABANDONED) {
            ttl = Math.min(ttl, 1000 * 60);
        }
    }

    public long getTTL() {
        return ttl;
    }
}