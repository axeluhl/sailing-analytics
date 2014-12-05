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

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((leaderboardName == null) ? 0 : leaderboardName.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        FlexibleLeaderboardAsRegattaLikeIdentifier other = (FlexibleLeaderboardAsRegattaLikeIdentifier) obj;
        if (leaderboardName == null) {
            if (other.leaderboardName != null)
                return false;
        } else if (!leaderboardName.equals(other.leaderboardName))
            return false;
        return true;
    }
}
