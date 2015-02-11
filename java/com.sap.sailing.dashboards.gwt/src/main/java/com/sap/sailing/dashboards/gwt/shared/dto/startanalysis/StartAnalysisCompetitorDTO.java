package com.sap.sailing.dashboards.gwt.shared.dto.startanalysis;

import java.io.Serializable;
import java.util.List;

import com.sap.sailing.domain.common.dto.CompetitorDTO;
import com.sap.sailing.gwt.ui.shared.GPSFixDTO;

public class StartAnalysisCompetitorDTO implements Serializable{
    
    private static final long serialVersionUID = -4236444628127858568L;
    
    public CompetitorDTO competitorDTO;
    public StartAnalysisRankingTableEntryDTO rankingTableEntryDTO;
    public List<GPSFixDTO> gpsFixDTOs;
}
