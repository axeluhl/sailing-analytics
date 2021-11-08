package com.sap.sailing.dashboards.gwt.shared.dto;

import java.io.Serializable;

import com.sap.sailing.domain.common.dto.BoatDTO;
import com.sap.sailing.domain.common.dto.CompetitorDTO;

public class StartAnalysisCompetitorDTO implements Serializable{
    
    private static final long serialVersionUID = -4236444628127858568L;
    
    public CompetitorDTO competitorDTO;
    public BoatDTO boatDTO;
    public StartAnalysisRankingTableEntryDTO rankingTableEntryDTO;
}
