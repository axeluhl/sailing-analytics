package com.sap.sailing.gwt.ui.adminconsole;

import java.util.UUID;

import com.sap.sailing.domain.common.ScoringSchemeType;

public class LeaderboardDescriptorWithOtherTieBreakingLeaderboard extends LeaderboardDescriptor {
    private String otherTieBreakingLeaderboardName;
    
    public LeaderboardDescriptorWithOtherTieBreakingLeaderboard() {
        super();
    }
    
    public LeaderboardDescriptorWithOtherTieBreakingLeaderboard(String name, String displayName, ScoringSchemeType scoringScheme,
            int[] discardThresholds, String regattaName, Iterable<UUID> courseAreaIds, String otherTieBreakingLeaderboardName) {
        super(name, displayName, scoringScheme, discardThresholds, regattaName, courseAreaIds);
        this.otherTieBreakingLeaderboardName = otherTieBreakingLeaderboardName;
    }
    
    public String getOtherTieBreakingLeaderboardName() {
        return otherTieBreakingLeaderboardName;
    }
}
