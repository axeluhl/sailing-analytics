package com.sap.sailing.gwt.ui.shared.dispatch.event;

import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gwt.core.shared.GwtIncompatible;
import com.sap.sailing.gwt.ui.shared.dispatch.Action;
import com.sap.sailing.gwt.ui.shared.dispatch.DispatchContext;
import com.sap.sailing.gwt.ui.shared.dispatch.ResultWithTTL;

public class GetLiveRacesForEventAction implements Action<ResultWithTTL<LiveRacesDTO>> {
    private static final Logger logger = Logger.getLogger(GetLiveRacesForEventAction.class.getName());
    
    private UUID eventId;
    
    public GetLiveRacesForEventAction() {
    }

    public GetLiveRacesForEventAction(UUID eventId) {
        this.eventId = eventId;
    }

    @Override
    @GwtIncompatible
    public ResultWithTTL<LiveRacesDTO> execute(DispatchContext context) {
        long start = System.currentTimeMillis();
        LiveRaceCalculator liveRaceCalculator = new LiveRaceCalculator();
        RacesActionUtil.forRacesOfEvent(context, eventId, liveRaceCalculator);
        ResultWithTTL<LiveRacesDTO> result = liveRaceCalculator.getResult();
        
        long duration = System.currentTimeMillis() - start;
        logger.log(Level.INFO, "Calculating live races for event "+ eventId + " took: "+ duration + "ms");
        return result;
    }
}
