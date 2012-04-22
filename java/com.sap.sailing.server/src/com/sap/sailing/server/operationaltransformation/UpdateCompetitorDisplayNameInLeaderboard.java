package com.sap.sailing.server.operationaltransformation;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.server.RacingEventService;

public class UpdateCompetitorDisplayNameInLeaderboard extends AbstractLeaderboardOperation {
    private static final long serialVersionUID = 366335484317671148L;
    private final String competitorIdAsString;
    private final String newDisplayName;
    
    public UpdateCompetitorDisplayNameInLeaderboard(String leaderboardName, String competitorIdAdString,
            String newDisplayName) {
        super(leaderboardName);
        this.competitorIdAsString = competitorIdAdString;
        this.newDisplayName = newDisplayName;
    }

    @Override
    public RacingEventServiceOperation transformClientOp(RacingEventServiceOperation serverOp) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public RacingEventServiceOperation transformServerOp(RacingEventServiceOperation clientOp) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public RacingEventService applyTo(RacingEventService toState) {
        Leaderboard leaderboard = toState.getLeaderboardByName(getLeaderboardName());
        Competitor competitor = leaderboard.getCompetitorByIdAsString(competitorIdAsString);
        if (competitor != null) {
            leaderboard.setDisplayName(competitor, newDisplayName);
            toState.updateStoredLeaderboard(leaderboard);
        }
        return toState;
    }

}
