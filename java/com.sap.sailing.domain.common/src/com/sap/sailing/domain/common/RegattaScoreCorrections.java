package com.sap.sailing.domain.common;

import java.util.Set;

public interface RegattaScoreCorrections {
    ScoreCorrectionProvider getProvider();
    
    String getRegattaName();
    
    /**
     * Lists the races in the order they are listed in the regatta, from first to last
     */
    Iterable<ScoreCorrectionsForRace> getScoreCorrectionsForRaces();
    
    public interface ScoreCorrectionsForRace {
        String getRaceNameOrNumber();
        
        Set<String> getSailIDs();
        
        ScoreCorrectionForCompetitorInRace getScoreCorrectionForCompetitor(String sailID);
    }
    
    public interface ScoreCorrectionForCompetitorInRace {
        /**
         * Must not return <code>null</code>
         */
        String getSailID();
        
        /**
         * @return an optional competitor name; may be <code>null</code> if not known
         */
        String getCompetitorName();
        
        /**
         * The points the competitor scored in the race
         */
        Double getPoints();
        
        /**
         * {@link MaxPointsReason#NONE} if no reason for disqualification or score correction is given, otherwise the reason
         * for disqualification / score correction
         */
        MaxPointsReason getMaxPointsReason();
        
        /**
         * @return <code>null</code> if it's not known whether the race was discarded or not; otherwise an authoritative
         *         statement about discarding
         */
        Boolean isDiscarded();
    }
}
