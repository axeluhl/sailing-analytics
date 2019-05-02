package com.sap.sailing.server.hierarchy;

import java.util.Set;

import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.leaderboard.LeaderboardGroup;

public interface EventHierarchyVisitor {

    void visit(LeaderboardGroup leaderboardGroup);

    void visit(Leaderboard leaderboard, Set<LeaderboardGroup> leaderboardGroups);
}
