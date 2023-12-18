package com.sap.sailing.datamining.impl.data;

import com.sap.sailing.datamining.data.HasCompetitorContext;
import com.sap.sailing.datamining.data.HasLeaderboardContext;
import com.sap.sailing.domain.base.Competitor;

public class CompetitorWithContext implements HasCompetitorContext {
    private final Competitor competitor;
    private final HasLeaderboardContext leaderboardContext;

    public CompetitorWithContext(Competitor competitor, HasLeaderboardContext leaderboardContext) {
        super();
        this.competitor = competitor;
        this.leaderboardContext = leaderboardContext;
    }

    @Override
    public Competitor getCompetitor() {
        return competitor;
    }

    @Override
    public HasLeaderboardContext getLeaderboardContext() {
        return leaderboardContext;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((competitor == null) ? 0 : competitor.hashCode());
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
        CompetitorWithContext other = (CompetitorWithContext) obj;
        if (competitor == null) {
            if (other.competitor != null)
                return false;
        } else if (!competitor.equals(other.competitor))
            return false;
        return true;
    }
}
