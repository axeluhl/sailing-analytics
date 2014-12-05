package com.sap.sailing.domain.regattalike;

import com.sap.sailing.domain.leaderboard.FlexibleLeaderboard;

public class FlexibleLeaderboardAsRegattaLikeIdentifier implements RegattaLikeIdentifier {
    private static final long serialVersionUID = -2770908733100377885L;
    private final String leaderboardName;
    
    public FlexibleLeaderboardAsRegattaLikeIdentifier(FlexibleLeaderboard leaderboard) {
        this.leaderboardName = leaderboard.getName();
    }

    @Override
    public String getName() {
        return leaderboardName;
    }

    @Override
    public void resolve(RegattaLikeIdentifierResolver resolver) {
        resolver.resolveOnFlexibleLeaderboardIdentifier(this);
    }

}
