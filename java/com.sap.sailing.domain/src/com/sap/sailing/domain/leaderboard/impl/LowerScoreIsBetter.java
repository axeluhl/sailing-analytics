package com.sap.sailing.domain.leaderboard.impl;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.common.MaxPointsReason;
import com.sap.sailing.domain.common.impl.Util;
import com.sap.sailing.domain.tracking.TrackedRace;


/**
 * The score comparator as used by the ISAF standard scoring scheme. Lower scores are better.
 * 
 * @author Axel Uhl (D043530)
 *
 */
public class LowerScoreIsBetter extends AbstractScoringSchemeImpl {
    private static final long serialVersionUID = -2767385186133743330L;

    public LowerScoreIsBetter() {
        super(/* higherIsBetter */ false);
    }

    @Override
    public Double getScoreForRank(RaceColumn raceColumn, Competitor competitor, int rank) {
        return (double) rank;
    }

    @Override
    public Double getPenaltyScore(RaceColumn raceColumn, Competitor competitor, MaxPointsReason maxPointsReason, int numberOfCompetitorsInLeaderboard) {
        Double result;
        final TrackedRace trackedRace = raceColumn.getTrackedRace(competitor);
        if (trackedRace == null) {
            result = (double) (numberOfCompetitorsInLeaderboard+1);
        } else {
            result = (double) (Util.size(trackedRace.getRace().getCompetitors())+1);
        }
        return result;
    }
}
