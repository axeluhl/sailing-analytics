package com.sap.sailing.server.operationaltransformation;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.server.RacingEventService;
import com.sap.sailing.server.RacingEventServiceOperation;

public class SetSuppressedFlagForCompetitorInLeaderboard extends AbstractLeaderboardOperation<Void> {
    private static final long serialVersionUID = -6509970065541824854L;
    private final String competitorIdAsString;
    private final boolean suppressed;

    public SetSuppressedFlagForCompetitorInLeaderboard(String leaderboardName, String competitorIdAsString, boolean suppressed) {
        super(leaderboardName);
        this.competitorIdAsString = competitorIdAsString;
        this.suppressed = suppressed;
    }

    @Override
    public Void internalApplyTo(RacingEventService toState) throws Exception {
        Leaderboard leaderboard = toState.getLeaderboardByName(getLeaderboardName());
        Competitor competitor = leaderboard.getCompetitorByIdAsString(competitorIdAsString);
        if (competitor != null) {
	        leaderboard.setSuppressed(competitor, suppressed);
	        toState.updateStoredLeaderboard(leaderboard);
        }
        return null;
    }

    @Override
    public RacingEventServiceOperation<?> transformClientOp(RacingEventServiceOperation<?> serverOp) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public RacingEventServiceOperation<?> transformServerOp(RacingEventServiceOperation<?> clientOp) {
        // TODO Auto-generated method stub
        return null;
    }

}
