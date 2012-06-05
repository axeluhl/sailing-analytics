package com.sap.sailing.server.operationaltransformation;

import com.sap.sailing.domain.leaderboard.FlexibleLeaderboard;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.leaderboard.RegattaLeaderboard;
import com.sap.sailing.domain.leaderboard.impl.RegattaLeaderboardImpl;
import com.sap.sailing.server.RacingEventService;



public abstract class AbstractLeaderboardOperation<ResultType> extends AbstractRacingEventServiceOperation<ResultType> {
    private static final long serialVersionUID = 5377910723455422127L;
    private final String leaderboardName;
    
    public AbstractLeaderboardOperation(String leaderboardName) {
        super();
        this.leaderboardName = leaderboardName;
    }

    protected String getLeaderboardName() {
        return leaderboardName;
    }

    protected boolean affectsSameLeaderboard(AbstractLeaderboardOperation<?> other) {
        return getLeaderboardName().equals(other.getLeaderboardName());
    }

    protected void updateStoredLeaderboard(RacingEventService toState, Leaderboard leaderboard) {
        if (leaderboard instanceof FlexibleLeaderboard) {
            toState.updateStoredFlexibleLeaderboard((FlexibleLeaderboard) leaderboard);
        } else if (leaderboard instanceof RegattaLeaderboardImpl) {
            toState.updateStoredRegattaLeaderboard((RegattaLeaderboard) leaderboard);
        }
    }
}
