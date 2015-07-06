package com.sap.sailing.gwt.ui.shared.dispatch.event;

import com.google.gwt.core.shared.GwtIncompatible;
import com.sap.sailing.gwt.ui.shared.dispatch.ResultWithTTL;
import com.sap.sailing.gwt.ui.shared.dispatch.event.EventActionUtil.RaceCallback;

@GwtIncompatible
public class RaceListDataCalculator implements RaceCallback {
    
    private final RaceListViewDTO result = new RaceListViewDTO();

    @Override
    public void doForRace(RaceContext context) {
        context.addLiveRace(result.getLiveRaces());
        
        RaceListRaceDTO finishedRace = context.getFinishedRaceOrNull();
        if(finishedRace != null) {
            result.addRace(finishedRace);
        }
    }
    
    public ResultWithTTL<RaceListViewDTO> getResult() {
        return new ResultWithTTL<>(5000, result);
    }

}
