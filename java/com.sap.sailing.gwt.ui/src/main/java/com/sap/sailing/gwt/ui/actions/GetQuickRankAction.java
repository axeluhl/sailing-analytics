package com.sap.sailing.gwt.ui.actions;

import java.util.Date;
import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.shared.QuickRankDTO;

public class GetQuickRankAction extends DefaultAsyncAction<List<QuickRankDTO>> {
    private final SailingServiceAsync sailingService;
    private final RegattaAndRaceIdentifier raceIdentifier;
    private final Date date;

    public GetQuickRankAction(SailingServiceAsync sailingService, RegattaAndRaceIdentifier raceIdentifier, Date date,
            AsyncCallback<List<QuickRankDTO>> callback) {
        super(callback);
        this.sailingService = sailingService;
        this.raceIdentifier = raceIdentifier;
        this.date = date;
    }
    
    @Override
    public void execute(AsyncActionsExecutor asyncActionsExecutor) {
        sailingService.getQuickRanks(raceIdentifier, date, (AsyncCallback<List<QuickRankDTO>>) getWrapperCallback(asyncActionsExecutor));
    }
}