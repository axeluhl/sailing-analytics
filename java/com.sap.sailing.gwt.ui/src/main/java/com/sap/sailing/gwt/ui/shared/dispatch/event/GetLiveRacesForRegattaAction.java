package com.sap.sailing.gwt.ui.shared.dispatch.event;

import java.util.UUID;

import com.google.gwt.core.shared.GwtIncompatible;
import com.sap.sailing.domain.base.Event;
import com.sap.sailing.gwt.ui.shared.dispatch.Action;
import com.sap.sailing.gwt.ui.shared.dispatch.DispatchContext;
import com.sap.sailing.gwt.ui.shared.dispatch.ResultWithTTL;
import com.sap.sailing.gwt.ui.shared.dispatch.event.EventActionUtil.CalculationWithEvent;

public class GetLiveRacesForRegattaAction implements Action<ResultWithTTL<LiveRacesDTO>> {
    private UUID eventId;
    private String regattaName;
    
    @SuppressWarnings("unused")
    private GetLiveRacesForRegattaAction() {
    }

    public GetLiveRacesForRegattaAction(UUID eventId, String regattaName) {
        this.eventId = eventId;
        this.regattaName = regattaName;
    }

    @Override
    @GwtIncompatible
    public ResultWithTTL<LiveRacesDTO> execute(final DispatchContext context) {
        return EventActionUtil.withLiveRaceOrDefaultSchedule(context, eventId, new CalculationWithEvent<LiveRacesDTO>() {
            @Override
            public ResultWithTTL<LiveRacesDTO> calculateWithEvent(Event event) {
                LiveRaceCalculator liveRaceCalculator = new LiveRaceCalculator();
                EventActionUtil.forRacesOfRegatta(context, eventId, regattaName, liveRaceCalculator);
                return liveRaceCalculator.getResult();
            }
        });
    }
}
