package com.sap.sailing.gwt.ui.raceboard;

import java.util.Date;
import java.util.Map;

import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.domain.common.dto.LeaderboardDTO;
import com.sap.sailing.gwt.ui.leaderboard.LeaderboardSettings;
import com.sap.sailing.gwt.ui.shared.RaceTimesInfoDTO;
import com.sap.sse.gwt.client.player.Timer.PlayStates;

/**
 * The start analysis mode makes the competitor chart visible and sets it to speed over ground; the
 * {@link LeaderboardSettings} are adjusted such that no leg columns but only start parameters are
 * shown. The top three starters are selected when the leaderboard has been updated after setting
 * the timer to a few seconds after the start. The {@link PlayStates#Paused} is used for the timer.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public class StartAnalysisMode extends AbstractRaceBoardMode {

    @Override
    public void raceTimesInfosReceived(Map<RegattaAndRaceIdentifier, RaceTimesInfoDTO> raceTimesInfo,
            long clientTimeWhenRequestWasSent, Date serverTimeDuringRequest, long clientTimeWhenResponseWasReceived) {
        // TODO Auto-generated method stub
        super.raceTimesInfosReceived(raceTimesInfo, clientTimeWhenRequestWasSent, serverTimeDuringRequest,
                clientTimeWhenResponseWasReceived);
    }

    @Override
    public void updatedLeaderboard(LeaderboardDTO leaderboard) {
        // TODO Auto-generated method stub
        super.updatedLeaderboard(leaderboard);
    }
}
