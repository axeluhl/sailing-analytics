package com.sap.sailing.domain.leaderboard.impl;

import java.util.function.Supplier;

import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.common.LeaderboardType;
import com.sap.sailing.domain.leaderboard.AbstractScoreCorrectionListenerWithDefaultAction;
import com.sap.sailing.domain.leaderboard.RegattaLeaderboard;
import com.sap.sailing.domain.leaderboard.RegattaLeaderboardWithOtherTieBreakingLeaderboard;
import com.sap.sailing.domain.leaderboard.ScoreCorrectionListener;
import com.sap.sailing.domain.leaderboard.ThresholdBasedResultDiscardingRule;

/**
 * Upon construction accepts a supplier for another {@link RegattaLeaderboard} and schedules the registration
 * of a {@link ScoreCorrectionListener} on that leaderboard. When score corrections change on that other
 * leaderboard, this leaderboard's cache contents are invalidated.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public class RegattaLeaderboardWithOtherTieBreakingLeaderboardImpl extends RegattaLeaderboardImpl
        implements RegattaLeaderboardWithOtherTieBreakingLeaderboard {
    private static final long serialVersionUID = -4859656155952136210L;
    private final DelegateLeaderboard otherTieBreakingLeaderboardProvider;

    public RegattaLeaderboardWithOtherTieBreakingLeaderboardImpl(Regatta regatta,
            ThresholdBasedResultDiscardingRule resultDiscardingRule, Supplier<RegattaLeaderboard> otherTieBreakingLeaderboardProvider) {
        super(regatta, resultDiscardingRule);
        this.otherTieBreakingLeaderboardProvider = new DelegateLeaderboard(otherTieBreakingLeaderboardProvider);
        this.otherTieBreakingLeaderboardProvider.runOrSchedule(otherTieBreakingLeaderboard->otherTieBreakingLeaderboard.addScoreCorrectionListener(
                new AbstractScoreCorrectionListenerWithDefaultAction() {
                    @Override
                    protected void defaultAction() {
                        getLeaderboardDTOCache().invalidate(RegattaLeaderboardWithOtherTieBreakingLeaderboardImpl.this);
                    }
                }));
    }

    @Override
    public RegattaLeaderboard getOtherTieBreakingLeaderboard() {
        return otherTieBreakingLeaderboardProvider.getDelegateLeaderboard();
    }
    
    @Override
    public LeaderboardType getLeaderboardType() {
        return LeaderboardType.RegattaLeaderboardWithOtherTieBreakingLeaderboard;
    }
}
