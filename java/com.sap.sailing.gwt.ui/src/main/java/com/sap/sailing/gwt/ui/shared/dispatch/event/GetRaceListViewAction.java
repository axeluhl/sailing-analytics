package com.sap.sailing.gwt.ui.shared.dispatch.event;

import java.util.UUID;

import com.google.gwt.core.shared.GwtIncompatible;
import com.sap.sailing.gwt.ui.shared.dispatch.Action;
import com.sap.sailing.gwt.ui.shared.dispatch.DispatchContext;
import com.sap.sailing.gwt.ui.shared.dispatch.ResultWithTTL;

public class GetRaceListViewAction implements Action<ResultWithTTL<RaceListViewDTO>> {
    
    private UUID eventId;
    private String regattaId;
    
    @SuppressWarnings("unused")
    private GetRaceListViewAction() {
    }

    public GetRaceListViewAction(UUID eventId, String regattaId) {
        this.eventId = eventId;
        this.regattaId = regattaId;
    }

    @Override
    @GwtIncompatible
    public ResultWithTTL<RaceListViewDTO> execute(DispatchContext context) {
        RaceListDataCalculator raceListDataCalculator = new RaceListDataCalculator();
        EventActionUtil.forRacesOfRegatta(context, eventId, regattaId, raceListDataCalculator);
        ResultWithTTL<RaceListViewDTO> result = raceListDataCalculator.getResult(context, eventId, regattaId);
        
        return result;
    }
}
