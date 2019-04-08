package com.sap.sailing.domain.leaderboard.impl;

import com.sap.sailing.domain.abstractlog.race.scoring.RaceLogAdditionalScoringInformationEvent;
import com.sap.sailing.domain.common.ScoringSchemeType;

/**
 * {@link HighPointFirstGetsFixedScore} scheme that in most cases applies
 * 12 points for the winner. Iff for one race column a {@link RaceLogAdditionalScoringInformationEvent}
 * is found then 8 points are applied for the winner.
 * 
 * From Phil, Race Director, on 12.09.2014: "If there is a tie in the regatta score between two or more boats at any time, the tie
 * shall be broken in favour of the boat that has won the most races. If a tie still remains, it shall be broken in
 * favour of the boat that had the better place at the last race sailed."
 * 
 * @author Simon Marcel Pamies
 */
public class HighPointFirstGets12Or8AndLastBreaksTie extends AbstractHighPointFirstGetsFixedOr8AndLastBreaksTie {
    private static final long serialVersionUID = 1L;
    
    public HighPointFirstGets12Or8AndLastBreaksTie() {
        super(12.0);
    }

    @Override
    public ScoringSchemeType getType() {
        return ScoringSchemeType.HIGH_POINT_FIRST_GETS_TWELVE_OR_EIGHT;
    }
}
