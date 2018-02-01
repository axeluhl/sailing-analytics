package com.sap.sailing.gwt.ui.shared;

import java.util.LinkedHashMap;
import java.util.Map;

import com.google.gwt.user.client.rpc.IsSerializable;
import com.sap.sailing.domain.common.MaxPointsReason;

public class RegattaScoreCorrectionDTO implements IsSerializable {
    public static class ScoreCorrectionEntryDTO implements IsSerializable {
        private Double score;
        private Boolean discarded;
        private MaxPointsReason maxPointsReason;
        
        public ScoreCorrectionEntryDTO() {}

        public ScoreCorrectionEntryDTO(Double score, Boolean discarded, MaxPointsReason maxPointsReason) {
            super();
            this.score = score;
            this.discarded = discarded;
            this.maxPointsReason = maxPointsReason;
        }

        public Double getScore() {
            return score;
        }

        public Boolean isDiscarded() {
            return discarded;
        }

        public MaxPointsReason getMaxPointsReason() {
            return maxPointsReason;
        }
    }
    
    private String providerName;
    
    /**
     * Key is the race name or number as String; values are maps whose key is the sailID.
     */
    private LinkedHashMap<String, Map<String, ScoreCorrectionEntryDTO>> scoreCorrectionsByRaceNameOrNumber;
    
    public RegattaScoreCorrectionDTO() {}
    
    /**
     * @param scoreCorrectionsByRaceNameOrNumber
     *            Key is the race name or number as String; values are maps whose key is the sailID.
     */
    public RegattaScoreCorrectionDTO(String providerName,
            LinkedHashMap<String, Map<String, ScoreCorrectionEntryDTO>> scoreCorrectionsByRaceNameOrNumber) {
        super();
        this.providerName = providerName;
        this.scoreCorrectionsByRaceNameOrNumber = scoreCorrectionsByRaceNameOrNumber;
    }

    public String getProviderName() {
        return providerName;
    }
    
    public LinkedHashMap<String, Map<String, ScoreCorrectionEntryDTO>> getScoreCorrectionsByRaceNameOrNumber() {
        return scoreCorrectionsByRaceNameOrNumber;
    }
}
