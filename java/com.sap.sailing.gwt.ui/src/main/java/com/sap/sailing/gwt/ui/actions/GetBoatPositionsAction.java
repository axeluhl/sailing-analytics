package com.sap.sailing.gwt.ui.actions;

import java.util.Date;
import java.util.List;
import java.util.Map;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sailing.domain.common.RaceIdentifier;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.shared.CompetitorDTO;
import com.sap.sailing.gwt.ui.shared.GPSFixDTO;


public class GetBoatPositionsAction extends DefaultAsyncAction<Map<CompetitorDTO, List<GPSFixDTO>>>
{
    private final SailingServiceAsync sailingService;
    private final RaceIdentifier raceIdentifier;
    private final Map<CompetitorDTO, Date> from;
    private final Map<CompetitorDTO, Date> to;
    private final boolean extrapolate;
    private Map<CompetitorDTO, List<GPSFixDTO>> result;
    
    private AsyncCallback<Map<CompetitorDTO, List<GPSFixDTO>>> callback;
    
    public GetBoatPositionsAction(SailingServiceAsync sailingService, RaceIdentifier raceIdentifier,
            Map<CompetitorDTO, Date> from, Map<CompetitorDTO, Date> to, boolean extrapolate) {
        this.sailingService = sailingService;
        this.raceIdentifier = raceIdentifier;
        this.from = from;
        this.to = to;
        this.extrapolate = extrapolate;
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public void execute() {
        sailingService.getBoatPositions(raceIdentifier, from, to, extrapolate, (AsyncCallback<Map<CompetitorDTO, List<GPSFixDTO>>>) wrapperCallback);
    }

    @Override
    public Map<CompetitorDTO, List<GPSFixDTO>> getResult() {
        return result;
    }

    @Override
    public AsyncCallback<Map<CompetitorDTO, List<GPSFixDTO>>> getCallback() {
        return callback;
    }

    @Override
    public void setCallback(AsyncCallback<Map<CompetitorDTO, List<GPSFixDTO>>> callback) {
        this.callback = callback;
    }

    @Override
    public String getName() {
        return GetBoatPositionsAction.class.getName();
    }
}
