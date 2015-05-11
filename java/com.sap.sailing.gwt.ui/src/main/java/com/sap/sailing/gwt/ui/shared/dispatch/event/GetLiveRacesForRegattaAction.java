package com.sap.sailing.gwt.ui.shared.dispatch.event;

import java.util.UUID;

import com.google.gwt.core.shared.GwtIncompatible;
import com.sap.sailing.gwt.ui.shared.dispatch.Action;
import com.sap.sailing.gwt.ui.shared.dispatch.DispatchContext;
import com.sap.sailing.gwt.ui.shared.dispatch.ResultWithTTL;
import com.sap.sailing.gwt.ui.shared.dispatch.event.RacesActionUtil.RaceCallback;

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
        final LiveRacesDTO result = new LiveRacesDTO();
        
        RacesActionUtil.forRacesOfRegatta(context, eventId, regattaName, new RaceCallback() {
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
