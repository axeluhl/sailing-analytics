package com.sap.sailing.gwt.ui.shared.dispatch.event;

import java.util.UUID;

import com.google.gwt.core.shared.GwtIncompatible;
import com.sap.sailing.gwt.ui.shared.dispatch.Action;
import com.sap.sailing.gwt.ui.shared.dispatch.DispatchContext;
import com.sap.sailing.gwt.ui.shared.dispatch.ResultWithTTL;

public class GetLiveRacesForEventAction implements Action<ResultWithTTL<LiveRacesDTO>> {
    private UUID eventId;
    
    @SuppressWarnings("unused")
    private GetLiveRacesForEventAction() {
    }

    public GetLiveRacesForEventAction(UUID eventId) {
        this.eventId = eventId;
    }

    @Override
    @GwtIncompatible
    public ResultWithTTL<LiveRacesDTO> execute(DispatchContext context) {
        LiveRaceCalculator liveRaceCalculator = new LiveRaceCalculator();
        RacesActionUtil.forRacesOfEvent(context, eventId, liveRaceCalculator);
        ResultWithTTL<LiveRacesDTO> result = liveRaceCalculator.getResult();
        return result;
    }
}
