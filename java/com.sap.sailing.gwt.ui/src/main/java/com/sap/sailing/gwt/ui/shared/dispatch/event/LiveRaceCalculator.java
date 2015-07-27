package com.sap.sailing.gwt.ui.shared.dispatch.event;

import com.google.gwt.core.shared.GwtIncompatible;
import com.sap.sailing.gwt.ui.shared.dispatch.ResultWithTTL;
import com.sap.sailing.gwt.ui.shared.dispatch.SortedSetResult;
import com.sap.sailing.gwt.ui.shared.dispatch.event.EventActionUtil.RaceCallback;

@GwtIncompatible
final class LiveRaceCalculator implements RaceCallback {
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
