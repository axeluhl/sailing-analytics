package com.sap.sailing.gwt.ui.shared.dispatch.event;

import java.util.UUID;

import com.google.gwt.core.shared.GwtIncompatible;
import com.sap.sailing.gwt.ui.shared.dispatch.Action;
import com.sap.sailing.gwt.ui.shared.dispatch.DispatchContext;
import com.sap.sailing.gwt.ui.shared.dispatch.ResultWithTTL;
import com.sap.sailing.gwt.ui.shared.dispatch.SortedSetResult;

public class GetFinishedRacesAction implements Action<ResultWithTTL<SortedSetResult<RaceListRaceDTO>>> {
    
    private UUID eventId;
    private String regattaId;
    
    @SuppressWarnings("unused")
    private GetFinishedRacesAction() {
    }

    public GetFinishedRacesAction(UUID eventId, String regattaId) {
        this.eventId = eventId;
        this.regattaId = regattaId;
    }

    @Override
    @GwtIncompatible
    public ResultWithTTL<SortedSetResult<RaceListRaceDTO>> execute(DispatchContext context) {
        RaceListDataCalculator raceListDataCalculator = new RaceListDataCalculator();
        EventActionUtil.forRacesOfRegatta(context, eventId, regattaId, raceListDataCalculator);
        
        return new ResultWithTTL<>(EventActionUtil.getEventStateDependentTTL(context, eventId, 5 * 60 * 1000),
                new SortedSetResult<>(raceListDataCalculator.getResult()));
    }
}
