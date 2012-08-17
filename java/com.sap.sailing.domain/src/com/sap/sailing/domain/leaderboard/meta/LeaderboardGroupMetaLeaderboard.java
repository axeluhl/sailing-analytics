package com.sap.sailing.domain.leaderboard.meta;

import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.leaderboard.LeaderboardGroup;
import com.sap.sailing.domain.leaderboard.ScoringScheme;
import com.sap.sailing.domain.leaderboard.ThresholdBasedResultDiscardingRule;

public class LeaderboardGroupMetaLeaderboard extends AbstractMetaLeaderboard {
    private static final long serialVersionUID = 8087872002175528002L;

    private final LeaderboardGroup leaderboardGroup;

    public LeaderboardGroupMetaLeaderboard(LeaderboardGroup leaderboardGroup, ScoringScheme scoringScheme,
            ThresholdBasedResultDiscardingRule resultDiscardingRule) {
        super(leaderboardGroup.getName(), scoringScheme, resultDiscardingRule);
        this.leaderboardGroup = leaderboardGroup;
    }

    @Override
    protected Iterable<Leaderboard> getLeaderboards() {
        return leaderboardGroup.getLeaderboards();
    }
    
}
