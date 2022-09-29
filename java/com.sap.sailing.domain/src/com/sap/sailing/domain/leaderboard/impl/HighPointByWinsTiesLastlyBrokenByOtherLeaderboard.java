package com.sap.sailing.domain.leaderboard.impl;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.common.ScoringSchemeType;
import com.sap.sailing.domain.leaderboard.RegattaLeaderboardWithOtherTieBreakingLeaderboard;
import com.sap.sse.common.TimePoint;

public class HighPointByWinsTiesLastlyBrokenByOtherLeaderboard extends HighPointMatchRacing {
    private static final long serialVersionUID = -2930982687072741643L;

    @Override
    public ScoringSchemeType getType() {
        return ScoringSchemeType.HIGH_POINT_BY_WINS_TIES_LASTLY_BROKEN_BY_OTHER_LEADERBOARD;
    }

    @Override
    public int compareByOtherTieBreakingLeaderboard(RegattaLeaderboardWithOtherTieBreakingLeaderboard leaderboard,
            Competitor o1, Competitor o2, TimePoint timePoint) {
        final int result;
        final int o1RankInOtherTieBreakingLeaderboard = leaderboard.getOtherTieBreakingLeaderboard().getTotalRankOfCompetitor(o1, timePoint);
        final int o2RankInOtherTieBreakingLeaderboard = leaderboard.getOtherTieBreakingLeaderboard().getTotalRankOfCompetitor(o2, timePoint);
        if (o1RankInOtherTieBreakingLeaderboard == o2RankInOtherTieBreakingLeaderboard) {
            result = 0;
        } else {
            if (o1RankInOtherTieBreakingLeaderboard == 0) {
                result = 1; // o1 has no rank; this is worse ("greater") than any valid rank
            } else if (o2RankInOtherTieBreakingLeaderboard == 0) {
                result = -1;
            } else {
                result = Integer.compare(o1RankInOtherTieBreakingLeaderboard, o2RankInOtherTieBreakingLeaderboard);
            }
        }
        return result;
    }
}
