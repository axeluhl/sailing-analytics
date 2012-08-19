package com.sap.sailing.domain.leaderboard.impl;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.common.MaxPointsReason;
import com.sap.sailing.domain.common.ScoringSchemeType;


/**
 * The scoring system as used by the ISAF standard scoring scheme, also known as the "Low Point Scoring System."
 * Scores are primarily attributed according to rank, so a race's winner gets score 1.00 and so on. Lower scores are
 * therefore better.
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
    public Double getScoreForRank(RaceColumn raceColumn, Competitor competitor, int rank, Integer numberOfCompetitorsInRace) {
        return rank == 0 ? null : (double) rank;
    }

    @Override
    public Double getPenaltyScore(RaceColumn raceColumn, Competitor competitor, MaxPointsReason maxPointsReason,
            Integer numberOfCompetitorsInRace, int numberOfCompetitorsInLeaderboard) {
        Double result;
        if (numberOfCompetitorsInRace == null) {
            result = (double) (numberOfCompetitorsInLeaderboard+1);
        } else {
            result = (double) (numberOfCompetitorsInRace+1);
        }
        return result;
    }

    @Override
    public ScoringSchemeType getType() {
        return ScoringSchemeType.LOW_POINT;
    }
}
