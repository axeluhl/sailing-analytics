package com.sap.sailing.gwt.home.server;

import com.google.gwt.core.shared.GwtIncompatible;
import com.sap.sailing.gwt.home.communication.event.LiveRaceDTO;
import com.sap.sailing.gwt.home.server.EventActionUtil.RaceCallback;
import com.sap.sse.gwt.dispatch.shared.commands.ResultWithTTL;
import com.sap.sse.gwt.dispatch.shared.commands.SortedSetResult;

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
