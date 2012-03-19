package com.sap.sailing.gwt.ui.actions;

import java.util.Date;
import java.util.Map;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sailing.domain.common.RaceIdentifier;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.shared.CompetitorDTO;
import com.sap.sailing.gwt.ui.shared.RaceMapDataDTO;

public class GetRaceMapDataAction extends DefaultAsyncAction<RaceMapDataDTO>
{
    private final SailingServiceAsync sailingService;
    private final RaceIdentifier raceIdentifier;
    private final Map<CompetitorDTO, Date> from;
    private final Map<CompetitorDTO, Date> to;
    private final boolean extrapolate;
    private final Date date;
    private RaceMapDataDTO result;
    
    private AsyncCallback<RaceMapDataDTO> callback;
   
    public GetRaceMapDataAction(SailingServiceAsync sailingService, RaceIdentifier raceIdentifier, Date date,
            Map<CompetitorDTO, Date> from, Map<CompetitorDTO, Date> to, boolean extrapolate) {
        this.sailingService = sailingService;
        this.raceIdentifier = raceIdentifier;
        this.date = date;
        this.from = from;
        this.to = to;
        this.extrapolate = extrapolate;
    }
    
    @Override
    public void execute() {
        sailingService.getRaceMapData(raceIdentifier, date, from, to, extrapolate, (AsyncCallback<RaceMapDataDTO>) wrapperCallback);
    }

    @Override
    public RaceMapDataDTO getResult() {
        return result;
    }

    @Override
    public AsyncCallback<RaceMapDataDTO> getCallback() {
        return callback;
    }

    @Override
    public void setCallback(AsyncCallback<RaceMapDataDTO> callback) {
        this.callback = callback;
    }

    @Override
    public String getName() {
        return GetRaceMapDataAction.class.getName();
    }
}
