package com.sap.sailing.gwt.ui.shared.dispatch.event;

import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gwt.core.shared.GwtIncompatible;
import com.sap.sailing.domain.base.Event;
import com.sap.sailing.gwt.ui.shared.dispatch.Action;
import com.sap.sailing.gwt.ui.shared.dispatch.DispatchContext;
import com.sap.sailing.gwt.ui.shared.dispatch.ResultWithTTL;
import com.sap.sailing.gwt.ui.shared.dispatch.event.EventActionUtil.CalculationWithEvent;

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
    public ResultWithTTL<LiveRacesDTO> execute(final DispatchContext context) {
        long start = System.currentTimeMillis();
        ResultWithTTL<LiveRacesDTO> result = EventActionUtil.withLiveRaceOrDefaultSchedule(context, eventId, new CalculationWithEvent<LiveRacesDTO>() {
            @Override
            public ResultWithTTL<LiveRacesDTO> calculateWithEvent(Event event) {
                LiveRaceCalculator liveRaceCalculator = new LiveRaceCalculator();
                EventActionUtil.forRacesOfEvent(context, eventId, liveRaceCalculator);
                ResultWithTTL<LiveRacesDTO> result = liveRaceCalculator.getResult();
                return result;
            }
        });
        
        long duration = System.currentTimeMillis() - start;
        logger.log(Level.INFO, "Calculating live races for event "+ eventId + " took: "+ duration + "ms");
        return result;
    }
}
