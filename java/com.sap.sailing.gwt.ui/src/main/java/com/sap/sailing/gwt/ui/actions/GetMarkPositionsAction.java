package com.sap.sailing.gwt.ui.actions;

import java.util.Date;
import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sailing.domain.common.RaceIdentifier;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.shared.MarkDTO;

public class GetMarkPositionsAction extends DefaultAsyncAction<List<MarkDTO>>
{
    private final SailingServiceAsync sailingService;
    private final RaceIdentifier raceIdentifier;
    private final Date date;
    private List<MarkDTO> result;
    
    private AsyncCallback<List<MarkDTO>> callback;

    public GetMarkPositionsAction(SailingServiceAsync sailingService, RaceIdentifier raceIdentifier, Date date) {
        this.sailingService = sailingService;
        this.raceIdentifier = raceIdentifier;
        this.date = date;
    }
    
    @Override
    public void execute() {
        sailingService.getMarkPositions(raceIdentifier, date, (AsyncCallback<List<MarkDTO>>) wrapperCallback);
    }

    @Override
    public List<MarkDTO> getResult() {
        return result;
    }

    @Override
    public AsyncCallback<List<MarkDTO>> getCallback() {
        return callback;
    }

    @Override
    public void setCallback(AsyncCallback<List<MarkDTO>> callback) {
        this.callback = callback;
    }

    @Override
    public String getName() {
        return GetMarkPositionsAction.class.getName();
    }
}