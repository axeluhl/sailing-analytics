package com.sap.sailing.ess40.resultimport.impl;

import java.util.logging.Logger;

import com.sap.sailing.domain.common.MaxPointsReason;
import com.sap.sailing.domain.common.RegattaScoreCorrections.ScoreCorrectionForCompetitorInRace;
import com.sap.sse.common.Util;

public class ScoreCorrectionForCompetitorInRaceImpl implements ScoreCorrectionForCompetitorInRace {
    private static final Logger logger = Logger.getLogger(ScoreCorrectionForCompetitorInRaceImpl.class.getName());
    
    private final String teamName;
    private final Double points;
    private final MaxPointsReason maxPointsReason;
    
    public ScoreCorrectionForCompetitorInRaceImpl(String teamName, Util.Pair<String, Double> rankAndPoints) {
        this.teamName = teamName;
        if (rankAndPoints == null) {
            points = 0.0;
            maxPointsReason = null;
        } else {
            MaxPointsReason mpe;
            try {
                Integer.valueOf(rankAndPoints.getA().trim());
                mpe = null;
            } catch (NumberFormatException nfe) {
                // no int; try parsing a MaxPointsReason
                try {
                    mpe = MaxPointsReason.valueOf(rankAndPoints.getA().trim());
                } catch (IllegalArgumentException iae) {
                    logger.info("Don't understand rank "+rankAndPoints.getA());
                    mpe = null;
                }
            }
            maxPointsReason = mpe;
            points = rankAndPoints.getB();
        }
    }

    @Override
    public String getSailID() {
        return teamName;
    }

    @Override
    public String getCompetitorName() {
        // TODO map team name to skipper name
        return teamName;
    }

    @Override
    public Double getPoints() {
        return points;
    }

    @Override
    public MaxPointsReason getMaxPointsReason() {
        return maxPointsReason;
    }

    /**
     * @return <code>false</code> because ESS doesn't have discards
     */
    @Override
    public Boolean isDiscarded() {
        return false;
    }

}
