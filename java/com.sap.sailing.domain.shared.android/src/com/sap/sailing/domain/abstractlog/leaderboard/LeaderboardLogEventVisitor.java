package com.sap.sailing.domain.abstractlog.leaderboard;



public interface LeaderboardLogEventVisitor {
    void visit(LeaderboardLogRevokeEvent event);
}
