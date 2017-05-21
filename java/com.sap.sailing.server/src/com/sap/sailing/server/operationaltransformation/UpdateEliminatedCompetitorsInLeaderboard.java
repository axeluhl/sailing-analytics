package com.sap.sailing.server.operationaltransformation;

import java.util.Set;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.server.RacingEventService;
import com.sap.sailing.server.RacingEventServiceOperation;

public class UpdateEliminatedCompetitorsInLeaderboard extends AbstractLeaderboardOperation<Void> {
    private static final long serialVersionUID = -5326008498204072454L;
    private final Set<Competitor> newEliminatedCompetitors;

    public UpdateEliminatedCompetitorsInLeaderboard(String leaderboardName,
            Set<Competitor> newEliminatedCompetitors) {
        super(leaderboardName);
        this.newEliminatedCompetitors = newEliminatedCompetitors;
    }

    @Override
    public Void internalApplyTo(RacingEventService toState) throws Exception {
        toState.setEliminatedCompetitor(getLeaderboardName(), newEliminatedCompetitors);
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
