package com.sap.sailing.gwt.home.server;

import com.google.gwt.core.shared.GwtIncompatible;
import com.sap.sailing.gwt.home.communication.event.LiveRaceDTO;
import com.sap.sailing.gwt.home.server.EventActionUtil.RaceCallback;
import com.sap.sse.gwt.dispatch.shared.commands.ResultWithTTL;
import com.sap.sse.gwt.dispatch.shared.commands.SortedSetResult;

/**
 * {@link RaceCallback} implementation, which collects the set of {@link RaceContext#getLiveRaceOrNull() live races}
 * and calculates a overall "time to live" for them using a {@link RaceRefreshCalculator}. 
 */
@GwtIncompatible
public final class LiveRaceCalculator implements RaceCallback {
    private final RaceRefreshCalculator refreshCalculator = new RaceRefreshCalculator();
    private final SortedSetResult<LiveRaceDTO> result = new SortedSetResult<LiveRaceDTO>();

    @Override
    public void doForRace(RaceContext rc) {
        result.addValue(rc.getLiveRaceOrNull());
        refreshCalculator.doForRace(rc);
    }

    /**
     * @return the {@link ResultWithTTL} containing the live races and their calculated overall "time to live".
     */
    public ResultWithTTL<SortedSetResult<LiveRaceDTO>> getResult() {
        return new ResultWithTTL<>(refreshCalculator.getTTL(), result);
    }
}
