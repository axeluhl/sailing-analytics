package com.sap.sailing.gwt.ui.actions;

import java.util.Collection;
import java.util.Date;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sailing.domain.common.dto.IncrementalOrFullLeaderboardDTO;
import com.sap.sailing.domain.common.dto.LeaderboardDTO;
import com.sap.sailing.gwt.ui.client.ErrorReporter;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;

/**
 * Performs a result adaptation from {@link IncrementalOrFullLeaderboardDTO} and {@link LeaderboardDTO}, encapsulating
 * the logic how differential, incremental leaderboard fetching and transmission works. This helps guarantee the
 * requirement of {@link IncrementalOrFullLeaderboardDTO#getLeaderboardDTO(LeaderboardDTO)} (only pass <code>null</code>
 * as the previous leaderboard if <code>null</code> was passed as identifier of the previous leaderboard to
 * {@link SailingServiceAsync#getLeaderboardByName(String, Date, Collection, String, AsyncCallback)} too.
 * 
 * @author Frank Mittag, Axel Uhl (d043530)
 * 
 */
public class GetLeaderboardByNameAction extends DefaultAsyncAction<LeaderboardDTO> {
    private final SailingServiceAsync sailingService;
    private final String leaderboardName;
    private final Date date;
    private final Collection<String> namesOfRacesForWhichToLoadLegDetails;
    private final LeaderboardDTO previousLeaderboard;
    private final StringMessages stringMessages;
    private final ErrorReporter errorReporter;
    
    public GetLeaderboardByNameAction(SailingServiceAsync sailingService, String leaderboardName, Date date,
            final Collection<String> namesOfRacesForWhichToLoadLegDetails, LeaderboardDTO previousLeaderboard,
            ErrorReporter errorReporter, StringMessages stringMessages, AsyncCallback<LeaderboardDTO> callback) {
        super(callback);
        this.sailingService = sailingService;
        this.errorReporter = errorReporter;
        this.stringMessages = stringMessages;
        this.leaderboardName = leaderboardName;
        this.date = date;
        this.namesOfRacesForWhichToLoadLegDetails = namesOfRacesForWhichToLoadLegDetails;
        this.previousLeaderboard = previousLeaderboard;
    }
    
    @Override
    public void execute(AsyncActionsExecutor asyncActionsExecutor) {
        final AsyncCallback<LeaderboardDTO> wrapperCallback = getWrapperCallback(asyncActionsExecutor);
        sailingService
                .getLeaderboardByName(leaderboardName, date, namesOfRacesForWhichToLoadLegDetails,
                        previousLeaderboard==null?null:previousLeaderboard.getId(),
                        new AsyncCallback<IncrementalOrFullLeaderboardDTO>() {
                            @Override
                            public void onFailure(Throwable caught) {
                                wrapperCallback.onFailure(caught);
                            }

                            @Override
                            public void onSuccess(IncrementalOrFullLeaderboardDTO result) {
                                if (result == null) {
                                    errorReporter.reportError(stringMessages.errorTryingToObtainLeaderboardContents(leaderboardName), /* silent */ true);
                                } else {
                                    LeaderboardDTO leaderboardDTOResult = result.getLeaderboardDTO(previousLeaderboard);
                                    wrapperCallback.onSuccess(leaderboardDTOResult);
                                }
                            }
                });
    }

}