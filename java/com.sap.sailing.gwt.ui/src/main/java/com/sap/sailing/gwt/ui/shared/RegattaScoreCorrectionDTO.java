package com.sap.sailing.gwt.ui.shared;

import java.util.Map;

import com.google.gwt.user.client.rpc.IsSerializable;

public class RegattaScoreCorrectionDTO implements IsSerializable {
    public class ScoreCorrectionEntryDTO implements IsSerializable {
        public ScoreCorrectionEntryDTO() {}
        
    }
    
    private String providerName;
    private Map<Integer, Map<CompetitorDTO, ScoreCorrectionEntryDTO>> scoreCorrectionsByRaceNumber;
    
    public RegattaScoreCorrectionDTO() {}
    
    public RegattaScoreCorrectionDTO(String providerName,
            Map<Integer, Map<CompetitorDTO, ScoreCorrectionEntryDTO>> scoreCorrectionsByRaceNumber) {
        super();
        this.providerName = providerName;
        this.scoreCorrectionsByRaceNumber = scoreCorrectionsByRaceNumber;
    }

    public String getProviderName() {
        return providerName;
    }
    
    public Map<Integer, Map<CompetitorDTO, ScoreCorrectionEntryDTO>> getScoreCorrectionsByRaceNumber() {
        return scoreCorrectionsByRaceNumber;
    }
}
