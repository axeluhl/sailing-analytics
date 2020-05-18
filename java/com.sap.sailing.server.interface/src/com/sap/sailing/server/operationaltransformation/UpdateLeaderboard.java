package com.sap.sailing.server.operationaltransformation;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import com.sap.sailing.domain.leaderboard.FlexibleLeaderboard;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.leaderboard.ThresholdBasedResultDiscardingRule;
import com.sap.sailing.domain.leaderboard.impl.ThresholdBasedResultDiscardingRuleImpl;
import com.sap.sailing.server.interfaces.RacingEventService;
import com.sap.sailing.server.interfaces.RacingEventServiceOperation;
import com.sap.sse.common.Util;

public class UpdateLeaderboard extends AbstractLeaderboardOperation<Leaderboard> {
    private static final long serialVersionUID = -8040361040050151768L;
    private final String newLeaderboardDisplayName;
    private final int[] newDiscardingThresholds;
    private final Collection<Serializable> newCourseAreaIds;
    
    public UpdateLeaderboard(String leaderboardName, String newLeaderboardDisplayName, int[] newDiscardingThresholds,
            Iterable<? extends Serializable> newCourseAreaIds) {
        super(leaderboardName);
        this.newLeaderboardDisplayName = newLeaderboardDisplayName;
        this.newDiscardingThresholds = newDiscardingThresholds;
        this.newCourseAreaIds = new ArrayList<>();
        Util.addAll(newCourseAreaIds, this.newCourseAreaIds);
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
        Leaderboard leaderboard = toState.getLeaderboardByName(getLeaderboardName());
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
            flexibleLeaderboard.setCourseAreas(Util.map(newCourseAreaIds, toState::getCourseArea));
        }
        updateStoredLeaderboard(toState, leaderboard);
        return leaderboard;
    }
}
