package com.sap.sailing.gwt.ui.shared.dispatch.event;

import java.util.UUID;

import com.google.gwt.core.shared.GwtIncompatible;
import com.sap.sailing.gwt.ui.shared.dispatch.Action;
import com.sap.sailing.gwt.ui.shared.dispatch.DispatchContext;
import com.sap.sailing.gwt.ui.shared.dispatch.ResultWithTTL;
import com.sap.sailing.gwt.ui.shared.dispatch.event.RacesActionUtil.RaceCallback;

public class GetLiveRacesForEventAction implements Action<ResultWithTTL<LiveRacesDTO>> {
    private UUID eventId;
    
    public GetLiveRacesForEventAction() {
    }

    public GetLiveRacesForEventAction(UUID eventId) {
        this.eventId = eventId;
    }

    @Override
    @GwtIncompatible
    public ResultWithTTL<LiveRacesDTO> execute(DispatchContext context) {
        final LiveRacesDTO result = new LiveRacesDTO();
        
        RacesActionUtil.forRacesOfEvent(context, eventId, new RaceCallback() {
            @Override
            public void doForRace(RaceContext rc) {
                // TODO better condition after Frank implemented race state stuff
                if(!rc.isRaceDefinitionAvailable() || !rc.isRaceLogAvailable() || !rc.isLive() || !rc.isStartTimeAvailable()) {
                    return;
                }
                
                result.addRace(rc.getLiveRaceDTO());
            }
        });
        return new ResultWithTTL<LiveRacesDTO>(5000, result);
    }
}
