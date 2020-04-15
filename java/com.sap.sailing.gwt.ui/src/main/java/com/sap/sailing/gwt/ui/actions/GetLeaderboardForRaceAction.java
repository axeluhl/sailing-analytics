package com.sap.sailing.gwt.ui.actions;

import java.util.Collection;
import java.util.Date;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.domain.common.dto.IncrementalOrFullLeaderboardDTO;
import com.sap.sailing.domain.common.dto.LeaderboardDTO;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.async.AsyncAction;
import com.sap.sse.gwt.client.player.Timer;

/**
 * Performs a result adaptation from {@link IncrementalOrFullLeaderboardDTO} and {@link LeaderboardDTO}, encapsulating
 * the logic how differential, incremental leaderboard fetching and transmission works. This helps guarantee the
 * requirement of {@link IncrementalOrFullLeaderboardDTO#getLeaderboardDTO(LeaderboardDTO)} (only pass <code>null</code>
 * as the previous leaderboard if <code>null</code> was passed as identifier of the previous leaderboard to
 * {@link SailingServiceAsync#getLeaderboardByName(String, Date, Collection, boolean, String, AsyncCallback)} too.
 * <p>
 * 
 * When a valid {@link IncrementalOrFullLeaderboardDTO} object is received, the
 * {@link IncrementalOrFullLeaderboardDTO#getCurrentServerTime() server time} is used to adjust a
 * {@link #timerToAdjustOffetIn timer} passed to the constructor, if it wasn't <code>null</code>.
 * 
 * @author Frank Mittag, Axel Uhl (d043530)
 * 
 */
public class GetLeaderboardForRaceAction implements AsyncAction<LeaderboardDTO> {
    private final SailingServiceAsync sailingService;
    private final String leaderboardName;
    private final RegattaAndRaceIdentifier identifier;
    private final Date date;
    private final Collection<String> namesOfRaceColumnsForWhichToLoadLegDetails;
    private final boolean addOverallDetails;
    private final LeaderboardDTO previousLeaderboard;
    private final StringMessages stringMessages;
    private final ErrorReporter errorReporter;
    private final Timer timerToAdjustOffetIn;
    private final boolean fillTotalPointsUncorrected;
    
    public GetLeaderboardForRaceAction(SailingServiceAsync sailingService, String leaderboardName,
            RegattaAndRaceIdentifier identifier, Date date,
            final Collection<String> namesOfRaceColumnsForWhichToLoadLegDetails, boolean addOverallDetails,
            LeaderboardDTO previousLeaderboard, boolean fillTotalPointsUncorrected, Timer timerToAdjustOffsetIn,
            ErrorReporter errorReporter, StringMessages stringMessages) {
        this.sailingService = sailingService;
        this.identifier = identifier;
        this.errorReporter = errorReporter;
        this.stringMessages = stringMessages;
        this.leaderboardName = leaderboardName;
        this.date = date;
        this.namesOfRaceColumnsForWhichToLoadLegDetails = namesOfRaceColumnsForWhichToLoadLegDetails;
        this.addOverallDetails = addOverallDetails;
        this.previousLeaderboard = previousLeaderboard;
        this.timerToAdjustOffetIn = timerToAdjustOffsetIn;
        this.fillTotalPointsUncorrected = fillTotalPointsUncorrected;
    }
    
    @Override
    public void execute(final AsyncCallback<LeaderboardDTO> callback) {
        final long clientTimeWhenRequestWasSent = System.currentTimeMillis();
        sailingService.getLeaderboardForRace(identifier, leaderboardName, date,
                namesOfRaceColumnsForWhichToLoadLegDetails, addOverallDetails,
                previousLeaderboard==null?null:previousLeaderboard.getId(), fillTotalPointsUncorrected,
                        new AsyncCallback<IncrementalOrFullLeaderboardDTO>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        callback.onFailure(caught);
                    }

                    @Override
                    public void onSuccess(IncrementalOrFullLeaderboardDTO result) {
                        final long clientTimeWhenResponseWasReceived = System.currentTimeMillis();
                        if (result == null) {
                            errorReporter.reportError(stringMessages.errorTryingToObtainLeaderboardContents(leaderboardName), /* silent */ true);
                        } else {
                            if (timerToAdjustOffetIn != null) {
                                timerToAdjustOffetIn.adjustClientServerOffset(clientTimeWhenRequestWasSent,
                                        result.getCurrentServerTime(), clientTimeWhenResponseWasReceived);
                            }
                            LeaderboardDTO leaderboardDTOResult = result.getLeaderboardDTO(previousLeaderboard);
                            callback.onSuccess(leaderboardDTOResult);
                        }
                    }
                });
    }

}