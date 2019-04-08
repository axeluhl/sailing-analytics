package com.sap.sailing.resultimport.impl;

import java.util.logging.Logger;

import com.sap.sailing.domain.common.MaxPointsReason;
import com.sap.sailing.domain.common.RegattaScoreCorrections.ScoreCorrectionForCompetitorInRace;
import com.sap.sailing.resultimport.CompetitorEntry;

public class ScoreCorrectionForCompetitorInRaceImpl implements ScoreCorrectionForCompetitorInRace {
    private static final Logger logger = Logger.getLogger(ScoreCorrectionForCompetitorInRaceImpl.class.getName());
    
    private final String competitorName;
    private final String sailID;
    private final int points;
    private final MaxPointsReason maxPointsReason;
    private final boolean discarded;
    
    public ScoreCorrectionForCompetitorInRaceImpl(String sailID, String competitorName, CompetitorEntry competitorEntry) {
        this.sailID = sailID;
        this.competitorName = competitorName;
        if (competitorEntry == null) {
            points = 0;
            maxPointsReason = null;
            discarded = false;
        } else {
            this.discarded = competitorEntry.isDiscarded();
            points = (int) (competitorEntry.getScore() == null ? 0 : (double) competitorEntry.getScore());
            MaxPointsReason mpe = null;
            if (competitorEntry.getMaxPointsReason() != null && competitorEntry.getMaxPointsReason().length() > 0) {
                // no int; try parsing a MaxPointsReason
                try {
                    mpe = MaxPointsReason.valueOf(competitorEntry.getMaxPointsReason().toUpperCase());
                } catch (IllegalArgumentException iae) {
                    logger.info("Don't understand rank "+competitorEntry.getMaxPointsReason());
                    mpe = null;
                }
            }
            maxPointsReason = mpe;
        }
    }

    @Override
    public String getSailID() {
        return sailID;
    }

    @Override
    public String getCompetitorName() {
        return competitorName;
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
        return discarded;
    }

}
