package com.sap.sailing.gwt.ui.shared.dispatch.event;

import com.google.gwt.core.shared.GwtIncompatible;
import com.sap.sailing.gwt.ui.shared.dispatch.ResultWithTTL;
import com.sap.sailing.gwt.ui.shared.dispatch.event.EventActionUtil.RaceCallback;

@GwtIncompatible
public class RaceListDataCalculator implements RaceCallback {
    
    private final RaceListViewDTO result = new RaceListViewDTO();

    @Override
    public void doForRace(RaceContext context) {
        result.add(context.getLiveRaceOrNull());
        result.add(context.getFinishedRaceOrNull());
    }
    
    public ResultWithTTL<RaceListViewDTO> getResult() {
        return new ResultWithTTL<>(5000, result);
    }

}
