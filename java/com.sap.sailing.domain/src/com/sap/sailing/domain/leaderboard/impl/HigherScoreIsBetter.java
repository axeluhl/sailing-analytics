package com.sap.sailing.domain.leaderboard.impl;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.common.MaxPointsReason;
import com.sap.sailing.domain.common.ScoringSchemeType;
import com.sap.sailing.domain.common.impl.Util;
import com.sap.sailing.domain.tracking.TrackedRace;


/**
 * The scoring system as used by the Extreme Sailing Series. Disqualified competitors get 0 points; the winner gets as
 * many points as there are competitors in the race. Higher scores are better.
 * 
 * @author Axel Uhl (D043530)
 * 
 */
public class HigherScoreIsBetter extends AbstractScoringSchemeImpl {
    private static final long serialVersionUID = -2767385186133743330L;

    public HigherScoreIsBetter() {
        super(/* higherIsBetter */ true);
    }

    @Override
    public Double getScoreForRank(RaceColumn raceColumn, Competitor competitor, int rank) {
        Double result;
        TrackedRace trackedRace = raceColumn.getTrackedRace(competitor);
        if (trackedRace == null) {
            result = null;
        } else {
            result = (double) (Util.size(trackedRace.getRace().getCompetitors())-rank+1);
        }
        return result;
    }

    @Override
    public Double getPenaltyScore(RaceColumn raceColumn, Competitor competitor, MaxPointsReason maxPointsReason, int numberOfCompetitorsInLeaderboard) {
        return 0.0;
    }

    @Override
    public ScoringSchemeType getType() {
        return ScoringSchemeType.HIGH_POINT;
    }
}
