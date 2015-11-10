package com.sap.sailing.gwt.home.server;

import com.google.gwt.core.shared.GwtIncompatible;
import com.sap.sailing.gwt.dispatch.client.ResultWithTTL;
import com.sap.sailing.gwt.dispatch.client.SortedSetResult;
import com.sap.sailing.gwt.home.communication.event.LiveRaceDTO;
import com.sap.sailing.gwt.home.server.EventActionUtil.RaceCallback;

@GwtIncompatible
public final class LiveRaceCalculator implements RaceCallback {
    private final RaceRefreshCalculator refreshCalculator = new RaceRefreshCalculator();
    private final SortedSetResult<LiveRaceDTO> result = new SortedSetResult<LiveRaceDTO>();

    @Override
    public void doForRace(RaceContext rc) {
        result.addValue(rc.getLiveRaceOrNull());
        refreshCalculator.doForRace(rc);
    }

    public ResultWithTTL<SortedSetResult<LiveRaceDTO>> getResult() {
        return new ResultWithTTL<>(refreshCalculator.getTTL(), result);
    }
}
