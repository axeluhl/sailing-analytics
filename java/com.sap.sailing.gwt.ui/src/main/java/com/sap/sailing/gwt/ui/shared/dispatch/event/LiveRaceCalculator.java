package com.sap.sailing.gwt.ui.shared.dispatch.event;

import com.google.gwt.core.shared.GwtIncompatible;
import com.sap.sailing.gwt.ui.shared.dispatch.ResultWithTTL;
import com.sap.sailing.gwt.ui.shared.dispatch.SortedSetResult;
import com.sap.sailing.gwt.ui.shared.dispatch.event.EventActionUtil.RaceCallback;
import com.sap.sailing.gwt.ui.shared.race.RaceMetadataDTO.RaceViewState;
import com.sap.sse.common.impl.MillisecondsTimePoint;

@GwtIncompatible
final class LiveRaceCalculator implements RaceCallback {
    private final SortedSetResult<LiveRaceDTO> result = new SortedSetResult<LiveRaceDTO>();

    @Override
    public void doForRace(RaceContext rc) {
        result.addValue(rc.getLiveRaceOrNull());
    }

    public ResultWithTTL<SortedSetResult<LiveRaceDTO>> getResult() {
        long ttl = 1000 * 60 * 2;
        for(LiveRaceDTO race : result.getValues()) {
            if(race.getViewState() == RaceViewState.RUNNING) {
                ttl = Math.min(ttl, 1000 * 30);
            } else if(race.getViewState() == RaceViewState.SCHEDULED) {
                int timeTillRace = (int) MillisecondsTimePoint.now().until(new MillisecondsTimePoint(race.getStart())).asMillis();
                ttl = Math.min(ttl, Math.min(1000 * 60, timeTillRace));
            } else if(race.getViewState() == RaceViewState.POSTPONED || race.getViewState() == RaceViewState.ABANDONED) {
                ttl = Math.min(ttl, 1000 * 60);
            }
        }
        return new ResultWithTTL<SortedSetResult<LiveRaceDTO>>(ttl, result);
    }
}