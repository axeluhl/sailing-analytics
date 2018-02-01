package com.sap.sailing.server.operationaltransformation;

import java.io.Serializable;
import java.util.Arrays;

import com.sap.sailing.domain.base.CourseArea;
import com.sap.sailing.domain.leaderboard.FlexibleLeaderboard;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.leaderboard.ThresholdBasedResultDiscardingRule;
import com.sap.sailing.domain.leaderboard.impl.ThresholdBasedResultDiscardingRuleImpl;
import com.sap.sailing.server.RacingEventService;
import com.sap.sailing.server.RacingEventServiceOperation;

public class UpdateLeaderboard extends AbstractLeaderboardOperation<Leaderboard> {
    private static final long serialVersionUID = -8040361040050151768L;
    private final String newLeaderboardName;
    private final String newLeaderboardDisplayName;
    private final int[] newDiscardingThresholds;
    private final Serializable newCourseAreaId;
    
    public UpdateLeaderboard(String leaderboardName, String newLeaderboardName, String newLeaderboardDisplayName, int[] newDiscardingThresholds, Serializable newCourseAreaId) {
        super(leaderboardName);
        this.newLeaderboardName = newLeaderboardName;
        this.newLeaderboardDisplayName = newLeaderboardDisplayName;
        this.newDiscardingThresholds = newDiscardingThresholds;
        this.newCourseAreaId = newCourseAreaId;
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
    public Leaderboard internalApplyTo(RacingEventService toState) {
        if (!getLeaderboardName().equals(newLeaderboardName)) {
            toState.renameLeaderboard(getLeaderboardName(), newLeaderboardName);
        }
        Leaderboard leaderboard = toState.getLeaderboardByName(newLeaderboardName);
        // If the new thresholds are null this means that the leaderboard is expected to obtain its result discarding
        // configuration from somewhere else implicitly, e.g., an underlying regatta, and we'll leave it alone;
        // Otherwise, a new threshold-based result discarding rule will be set based on the newDiscardingThresholds
        // unless the leaderboard already has an equal definition.
        if (newDiscardingThresholds != null
                && (!(leaderboard.getResultDiscardingRule() instanceof ThresholdBasedResultDiscardingRule) || !Arrays
                        .equals(((ThresholdBasedResultDiscardingRule) leaderboard.getResultDiscardingRule())
                                .getDiscardIndexResultsStartingWithHowManyRaces(), newDiscardingThresholds))) {
            leaderboard.setCrossLeaderboardResultDiscardingRule(new ThresholdBasedResultDiscardingRuleImpl(newDiscardingThresholds));
        }
        leaderboard.setDisplayName(newLeaderboardDisplayName);
        
        if (leaderboard instanceof FlexibleLeaderboard) {
            FlexibleLeaderboard flexibleLeaderboard = (FlexibleLeaderboard) leaderboard;
            CourseArea newCourseArea = toState.getCourseArea(newCourseAreaId);
            if (newCourseArea != flexibleLeaderboard.getDefaultCourseArea()) {
                flexibleLeaderboard.setDefaultCourseArea(newCourseArea);
            }
        }
        updateStoredLeaderboard(toState, leaderboard);
        return leaderboard;
    }

}
