package com.sap.sailing.domain.leaderboard.impl;

import com.sap.sailing.domain.abstractlog.race.scoring.RaceLogAdditionalScoringInformationEvent;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.common.MaxPointsReason;
import com.sap.sailing.domain.common.ScoringSchemeType;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.leaderboard.NumberOfCompetitorsInLeaderboardFetcher;
import com.sap.sse.common.TimePoint;

/**
 * {@link HighPointFirstGetsFixedScore} scheme that in most cases applies
 * 12 points for the winner. Iff for one race column a {@link RaceLogAdditionalScoringInformationEvent}
 * is found then 8 points are applied for the winner.
 * 
 * From Phil, Race Director, on 12.09.2014: "If there is a tie in the regatta score between two or more boats at any time, the tie
 * shall be broken in favour of the boat that has won the most races. If a tie still remains, it shall be broken in
 * favour of the boat that had the better place at the last race sailed."
 * 
 * 13.1.2  A Boat that did not start, did not finish, or retired shall receive 1 point less than the last boat to complete the race.
 * 13.1.3  A Boat that is disqualified from a race, including OCS, shall receive 2 points less than the last boat to complete the race.
 * 
 * @author Simon Marcel Pamies
 */
public class HighPointFirstGets12Or8AndLastBreaksTie2017 extends AbstractHighPointFirstGetsFixedOr8AndLastBreaksTie {
    private static final long serialVersionUID = 1L;
    
    public HighPointFirstGets12Or8AndLastBreaksTie2017() {
        super(12.0);
    }

    @Override
    public ScoringSchemeType getType() {
        return ScoringSchemeType.HIGH_POINT_FIRST_GETS_TWELVE_OR_EIGHT_2017;
    }
    
    @Override
    public Double getPenaltyScore(RaceColumn raceColumn, Competitor competitor, MaxPointsReason maxPointsReason,
            Integer numberOfCompetitorsInRace, NumberOfCompetitorsInLeaderboardFetcher numberOfCompetitorsInLeaderboardFetcher, TimePoint timePoint, Leaderboard leaderboard) {
        Double result;
        /**
         * 1. Get all competitors without a max point reason.
         * 2. This list of competitors is assumed to be all competitors having successfully taken part in the race.
         * 3. The score of the last competitor will be 12-size(competitorList)
         * 
         * XXX How about two competitors not finishing a race? They will get the same score. NOR does not state
         * anything on how to handle such a situation except that it implicates that this will lead to a tie. No
         * update from OC Sport on that question so far (Mar 2017).
         */
        int competitorCountWithoutMaxPointReason = numberOfCompetitorsInLeaderboardFetcher.getNumberOfCompetitorsWithoutMaxPointReason(raceColumn, timePoint);
        if (maxPointsReason == MaxPointsReason.DNS || maxPointsReason == MaxPointsReason.DNF || maxPointsReason == MaxPointsReason.RET) {
        	result = (12 - competitorCountWithoutMaxPointReason) * 1.0;
        } else if (maxPointsReason == MaxPointsReason.DSQ || maxPointsReason == MaxPointsReason.OCS) {
        	result = (12 - competitorCountWithoutMaxPointReason - 1) * 1.0;
        } else {
        	result = 0.0;
        }
        return result;
    }

}
