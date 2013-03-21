package com.sap.sailing.gwt.ui.actions;

import java.util.Date;
import java.util.Map;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.shared.CompetitorDTO;
import com.sap.sailing.gwt.ui.shared.RaceMapDataDTO;

public class GetRaceMapDataAction extends DefaultAsyncAction<RaceMapDataDTO> {
    private final SailingServiceAsync sailingService;
    private final RegattaAndRaceIdentifier raceIdentifier;
    private final Map<CompetitorDTO, Date> from;
    private final Map<CompetitorDTO, Date> to;
    private final boolean extrapolate;
    private final Date date;
   
    public GetRaceMapDataAction(SailingServiceAsync sailingService, RegattaAndRaceIdentifier raceIdentifier, Date date,
            Map<CompetitorDTO, Date> from, Map<CompetitorDTO, Date> to, boolean extrapolate,
            AsyncCallback<RaceMapDataDTO> callback) {
        super(callback);
        this.sailingService = sailingService;
        this.raceIdentifier = raceIdentifier;
        this.date = date;
        this.from = from;
        this.to = to;
        this.extrapolate = extrapolate;
    }
    
    @Override
    public void execute(AsyncActionsExecutor asyncActionsExecutor) {
        sailingService.getRaceMapData(raceIdentifier, date, from, to, extrapolate, (AsyncCallback<RaceMapDataDTO>) getWrapperCallback(asyncActionsExecutor));
    }
}
