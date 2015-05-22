package com.sap.sailing.gwt.ui.shared.dispatch.event;

import java.util.UUID;

import com.google.gwt.core.shared.GwtIncompatible;
import com.sap.sailing.gwt.ui.shared.dispatch.Action;
import com.sap.sailing.gwt.ui.shared.dispatch.DispatchContext;
import com.sap.sailing.gwt.ui.shared.dispatch.ResultWithTTL;

public class GetLiveRacesForRegattaAction implements Action<ResultWithTTL<LiveRacesDTO>> {
    private UUID eventId;
    private String regattaName;
    
    public GetLiveRacesForRegattaAction() {
    }

    public GetLiveRacesForRegattaAction(UUID eventId, String regattaName) {
        this.eventId = eventId;
        this.regattaName = regattaName;
    }

    @Override
    @GwtIncompatible
    public ResultWithTTL<LiveRacesDTO> execute(DispatchContext context) {
        LiveRaceCalculator liveRaceCalculator = new LiveRaceCalculator();
        RacesActionUtil.forRacesOfRegatta(context, eventId, regattaName, liveRaceCalculator);
        return liveRaceCalculator.getResult();
    }
}
