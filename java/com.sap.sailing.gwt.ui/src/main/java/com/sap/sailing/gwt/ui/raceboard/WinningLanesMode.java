package com.sap.sailing.gwt.ui.raceboard;

import java.util.Date;
import java.util.Map;

import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.domain.common.dto.LeaderboardDTO;
import com.sap.sailing.gwt.ui.shared.RaceTimesInfoDTO;

/**
 * Puts the race viewer in a mode where the user can see what may be called the "Winning Lanes." For this,
 * the timer is set to the point in time when the first competitor finishes the race, or, for live races,
 * to the current point in time. The tail length is chosen such that it covers the full track of the
 * competitor farthest ahead.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public class WinningLanesMode extends AbstractRaceBoardMode {

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
