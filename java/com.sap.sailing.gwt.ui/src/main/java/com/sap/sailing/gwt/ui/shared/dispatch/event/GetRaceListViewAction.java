package com.sap.sailing.gwt.ui.shared.dispatch.event;

import java.util.UUID;

import com.google.gwt.core.shared.GwtIncompatible;
import com.sap.sailing.gwt.ui.shared.dispatch.Action;
import com.sap.sailing.gwt.ui.shared.dispatch.DispatchContext;
import com.sap.sailing.gwt.ui.shared.dispatch.ResultWithTTL;

public class GetRaceListViewAction implements Action<ResultWithTTL<RaceListViewDTO>> {
    public GetRaceListViewAction() {
    }

    public GetRaceListViewAction(UUID eventId) {
    }

    @Override
    @GwtIncompatible
    public ResultWithTTL<RaceListViewDTO> execute(DispatchContext context) {
        final RaceListViewDTO result = new RaceListViewDTO();
        
//        RacesActionUtil.forRacesOfEvent(context, eventId, new RaceCallback() {
//            @Override
//            public void doForRace(RaceContext rc) {
//                // TODO better condition after Frank implemented race state stuff
//                if(!rc.isRaceDefinitionAvailable() || !rc.isRaceLogAvailable() || !rc.isLive() || !rc.isStartTimeAvailable()) {
//                    return;
//                }
//                
//                result.addRace(rc.getLiveRaceDTO());
//            }
//        });
        return new ResultWithTTL<>(5000, result);
    }
}
