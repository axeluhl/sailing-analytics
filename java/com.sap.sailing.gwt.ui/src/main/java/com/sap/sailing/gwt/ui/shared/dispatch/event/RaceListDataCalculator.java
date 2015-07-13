package com.sap.sailing.gwt.ui.shared.dispatch.event;

import java.util.UUID;

import com.google.gwt.core.shared.GwtIncompatible;
import com.sap.sailing.gwt.ui.shared.dispatch.DispatchContext;
import com.sap.sailing.gwt.ui.shared.dispatch.ResultWithTTL;
import com.sap.sailing.gwt.ui.shared.dispatch.event.EventActionUtil.RaceCallback;

@GwtIncompatible
public class RaceListDataCalculator implements RaceCallback {
    
    private final RegattaProgressCalculator progressCalculator = new RegattaProgressCalculator();
    private final RaceListViewDTO result = new RaceListViewDTO();

    @Override
    public void doForRace(RaceContext context) {
        progressCalculator.doForRace(context);
        result.add(context.getLiveRaceOrNull());
        result.add(context.getFinishedRaceOrNull());
    }
    
    public ResultWithTTL<RaceListViewDTO> getResult(DispatchContext context, UUID eventId) {
        result.setProgress(progressCalculator.getResult());
        return new ResultWithTTL<>(EventActionUtil.getEventStateDependentTTL(context, eventId, 3 * 60 * 1000), result);
    }

}
