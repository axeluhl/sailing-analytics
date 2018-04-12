package com.sap.sailing.dashboards.gwt.shared.dto;

import java.io.Serializable;

import com.sap.sailing.domain.common.dto.CompetitorWithBoatDTO;

public class StartAnalysisCompetitorDTO implements Serializable{
    
    private static final long serialVersionUID = -4236444628127858568L;
    
    public CompetitorWithBoatDTO competitorDTO;
    public StartAnalysisRankingTableEntryDTO rankingTableEntryDTO;
}
