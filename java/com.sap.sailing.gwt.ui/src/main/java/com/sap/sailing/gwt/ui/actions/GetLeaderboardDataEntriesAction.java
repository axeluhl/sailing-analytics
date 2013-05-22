package com.sap.sailing.gwt.ui.actions;

import java.util.Date;
import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sailing.domain.common.DetailType;
import com.sap.sailing.domain.common.dto.CompetitorDTO;
import com.sap.sailing.domain.common.impl.Util.Triple;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;

public class GetLeaderboardDataEntriesAction extends DefaultAsyncAction<List<Triple<String, List<CompetitorDTO>, List<Double>>>> {
    private final SailingServiceAsync sailingService;
    private final DetailType detailType;
    private final String leaderboardName;
    private final Date date;
    
    public GetLeaderboardDataEntriesAction(SailingServiceAsync sailingService, String leaderboardName, Date date, DetailType detailType,
            AsyncCallback<List<Triple<String, List<CompetitorDTO>, List<Double>>>> callback) {
        super(callback);
        this.sailingService = sailingService;
        this.leaderboardName = leaderboardName;
        this.date = date;
        this.detailType = detailType;
    }

    @Override
    public void execute(AsyncActionsExecutor asyncActionsExecutor) {
        sailingService.getLeaderboardDataEntriesForAllRaceColumns(leaderboardName, date, detailType, 
                (AsyncCallback<List<Triple<String, List<CompetitorDTO>, List<Double>>>>) getWrapperCallback(asyncActionsExecutor));
    }
}
