package com.sap.sailing.server.operationaltransformation;

import com.sap.sailing.domain.abstractlog.regatta.RegattaLog;
import com.sap.sailing.domain.leaderboard.FlexibleLeaderboard;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.server.interfaces.RacingEventService;

public abstract class AbstractRegattaLogOnFlexibleLeaderboardOperation<T> extends AbstractRegattaLogOperation<T> {
    private static final long serialVersionUID = -1811293351556698801L;
    
    public AbstractRegattaLogOnFlexibleLeaderboardOperation(String leaderboardName) {
        super(leaderboardName);
    }
    
    @Override
    protected RegattaLog getRegattaLog(RacingEventService toState) throws CouldNotResolveRegattaLogException {
        Leaderboard leaderboard = toState.getLeaderboardByName(regattaLikeParentName);
        if (! (leaderboard instanceof FlexibleLeaderboard)) {
            throw new CouldNotResolveRegattaLogException();
        }
        return ((FlexibleLeaderboard) leaderboard).getRegattaLike().getRegattaLog();
    }
}
