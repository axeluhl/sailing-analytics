package com.sap.sailing.server.operationaltransformation;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.server.RacingEventService;
import com.sap.sailing.server.RacingEventServiceOperation;

public class UpdateLeaderboardCarryValue extends AbstractLeaderboardOperation<Void> {
    private static final long serialVersionUID = 5467731814788887426L;
    private final String competitorIdAsString;
    private final Double newCarriedValue;
    
    public UpdateLeaderboardCarryValue(String leaderboardName, String competitorIdAsString, Double newCarriedValue) {
        super(leaderboardName);
        this.competitorIdAsString = competitorIdAsString;
        this.newCarriedValue = newCarriedValue;
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

    @Override
    public Void internalApplyTo(RacingEventService toState) {
        Leaderboard leaderboard = toState.getLeaderboardByName(getLeaderboardName());
        if (leaderboard != null) {
            Competitor competitor = leaderboard.getCompetitorByIdAsString(competitorIdAsString);
            if (competitor != null) {
                if (newCarriedValue == null) {
                    leaderboard.unsetCarriedPoints(competitor);
                } else {
                    leaderboard.setCarriedPoints(competitor, newCarriedValue);
                }
                updateStoredLeaderboard(toState, leaderboard);
            } else {
                throw new IllegalArgumentException("Didn't find competitor ID "+competitorIdAsString+" in leaderboard "+getLeaderboardName());
            }
        } else {
            throw new IllegalArgumentException("Didn't find leaderboard "+getLeaderboardName());
        }
        return null;
    }

}
