package com.sap.sailing.gwt.ui.shared.dispatch.event;

import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gwt.core.shared.GwtIncompatible;
import com.sap.sailing.gwt.ui.shared.dispatch.Action;
import com.sap.sailing.gwt.ui.shared.dispatch.DispatchContext;
import com.sap.sailing.gwt.ui.shared.dispatch.ResultWithTTL;

public class GetRaceListViewAction implements Action<ResultWithTTL<RaceListViewDTO>> {
    
    private static final Logger logger = Logger.getLogger(GetRaceListViewAction.class.getName());
    
    private UUID eventId;
    private String regattaId;
    
    public GetRaceListViewAction() {
    }

    public GetRaceListViewAction(UUID eventId, String regattaId) {
        this.eventId = eventId;
    }

    @Override
    @GwtIncompatible
    public ResultWithTTL<RaceListViewDTO> execute(DispatchContext context) {
        long start = System.currentTimeMillis();
        RaceListDataCalculator raceListDataCalculator = new RaceListDataCalculator();
        EventActionUtil.forRacesOfRegatta(context, eventId, regattaId, raceListDataCalculator);
        ResultWithTTL<RaceListViewDTO> result = raceListDataCalculator.getResult();
        
        long duration = System.currentTimeMillis() - start;
        logger.log(Level.INFO, "Calculating race list for event "+ eventId + " took: "+ duration + "ms");
        return result;
    }
}
