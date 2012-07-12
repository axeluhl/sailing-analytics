package com.sap.sailing.freg.resultimport.impl;

import java.util.logging.Logger;

import com.sap.sailing.domain.common.MaxPointsReason;
import com.sap.sailing.domain.common.RegattaScoreCorrections.ScoreCorrectionForCompetitorInRace;
import com.sap.sailing.domain.common.impl.Util.Pair;

public class ScoreCorrectionForCompetitorInRaceImpl implements ScoreCorrectionForCompetitorInRace {
    private static final Logger logger = Logger.getLogger(ScoreCorrectionForCompetitorInRaceImpl.class.getName());
    
    private final String teamName;
    private final int points;
    private final MaxPointsReason maxPointsReason;
    
    public ScoreCorrectionForCompetitorInRaceImpl(String teamName, int points, MaxPointsReason maxPointsReason) {
        super();
        this.teamName = teamName;
        this.points = points;
        this.maxPointsReason = maxPointsReason;
    }

    public ScoreCorrectionForCompetitorInRaceImpl(String teamName, Pair<String, Integer> rankAndPoints) {
        this.teamName = teamName;
        if (rankAndPoints == null) {
            points = 0;
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
        return (double) points;
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
