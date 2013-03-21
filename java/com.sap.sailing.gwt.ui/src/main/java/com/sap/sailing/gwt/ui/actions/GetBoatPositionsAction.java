package com.sap.sailing.gwt.ui.actions;

import java.util.Date;
import java.util.List;
import java.util.Map;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.shared.CompetitorDTO;
import com.sap.sailing.gwt.ui.shared.GPSFixDTO;


public class GetBoatPositionsAction extends DefaultAsyncAction<Map<CompetitorDTO, List<GPSFixDTO>>> {
    private final SailingServiceAsync sailingService;
    private final RegattaAndRaceIdentifier raceIdentifier;
    private final Map<CompetitorDTO, Date> from;
    private final Map<CompetitorDTO, Date> to;
    private final boolean extrapolate;
    
    
    public GetBoatPositionsAction(SailingServiceAsync sailingService, RegattaAndRaceIdentifier raceIdentifier,
            Map<CompetitorDTO, Date> from, Map<CompetitorDTO, Date> to, boolean extrapolate,
            AsyncCallback<Map<CompetitorDTO, List<GPSFixDTO>>> callback) {
        super(callback);
        this.sailingService = sailingService;
        this.raceIdentifier = raceIdentifier;
        this.from = from;
        this.to = to;
        this.extrapolate = extrapolate;
    }
    
    @Override
    public void execute(AsyncActionsExecutor asyncActionsExecutor) {
        sailingService.getBoatPositions(raceIdentifier, from, to, extrapolate,
                (AsyncCallback<Map<CompetitorDTO, List<GPSFixDTO>>>) getWrapperCallback(asyncActionsExecutor));
    }

}
