package com.sap.sailing.server.operationaltransformation;

import java.util.HashSet;
import java.util.Set;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.leaderboard.RegattaLeaderboardWithEliminations;
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
        // important not to manipulate the operation; create a local copy of the competitors before alterint it; see bug 4247
        final Set<Competitor> myNewEliminatedCompetitors = new HashSet<>(newEliminatedCompetitors);
        RegattaLeaderboardWithEliminations leaderboard = (RegattaLeaderboardWithEliminations) toState.getLeaderboardByName(getLeaderboardName());
        // first un-eliminate those currently eliminated and no longer in newEliminatedCompetitors
        for (final Competitor c : leaderboard.getEliminatedCompetitors()) {
            leaderboard.setEliminated(c, myNewEliminatedCompetitors.remove(c));
        }
        // then eliminated the remaining ones from newEliminatedCompetitors
        for (final Competitor c : myNewEliminatedCompetitors) {
            leaderboard.setEliminated(c, true);
        }
        updateStoredLeaderboard(toState, leaderboard);
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
