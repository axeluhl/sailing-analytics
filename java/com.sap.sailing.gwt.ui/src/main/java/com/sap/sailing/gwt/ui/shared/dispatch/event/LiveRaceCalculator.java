package com.sap.sailing.gwt.ui.shared.dispatch.event;

import com.google.gwt.core.shared.GwtIncompatible;
import com.sap.sailing.gwt.ui.shared.dispatch.ResultWithTTL;
import com.sap.sailing.gwt.ui.shared.dispatch.event.RacesActionUtil.RaceCallback;
import com.sap.sailing.gwt.ui.shared.race.RaceMetadataDTO.RaceViewState;
import com.sap.sse.common.impl.MillisecondsTimePoint;

@GwtIncompatible
final class LiveRaceCalculator implements RaceCallback {
    private final LiveRacesDTO result = new LiveRacesDTO();

    @Override
    public void doForRace(RaceContext rc) {
        rc.addLiveRace(result);
    }

    public ResultWithTTL<LiveRacesDTO> getResult() {
        long ttl = 1000 * 60 * 2;
        for(LiveRaceDTO race : result.getRaces()) {
            if(race.getViewState() == RaceViewState.RUNNING) {
                ttl = Math.min(ttl, 1000 * 30);
            } else if(race.getViewState() == RaceViewState.SCHEDULED) {
                int timeTillRace = (int) MillisecondsTimePoint.now().until(new MillisecondsTimePoint(race.getStart())).asMillis();
                ttl = Math.min(ttl, Math.min(1000 * 60, timeTillRace));
            } else if(race.getViewState() == RaceViewState.POSTPONED || race.getViewState() == RaceViewState.CANCELED) {
                ttl = Math.min(ttl, 1000 * 60);
            }
        }
        return new ResultWithTTL<LiveRacesDTO>(ttl, result);
    }
}