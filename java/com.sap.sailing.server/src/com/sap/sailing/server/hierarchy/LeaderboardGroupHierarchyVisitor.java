package com.sap.sailing.server.hierarchy;

import com.sap.sailing.domain.base.Event;
import com.sap.sailing.domain.leaderboard.Leaderboard;

public interface LeaderboardGroupHierarchyVisitor {

    void visit(Event event);

    void visit(Leaderboard leaderboard);
}
