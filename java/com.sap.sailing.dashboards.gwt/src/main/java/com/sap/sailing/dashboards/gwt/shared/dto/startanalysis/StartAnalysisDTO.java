package com.sap.sailing.dashboards.gwt.shared.dto.startanalysis;

import java.io.Serializable;
import java.util.List;

import com.sap.sailing.domain.common.dto.PositionDTO;
import com.sap.sailing.gwt.ui.shared.MarkDTO;

public class StartAnalysisDTO implements Serializable, Comparable<StartAnalysisDTO>{
    
    private static final long serialVersionUID = -1325150193180234561L;
    
    public String raceName;
    public List<PositionDTO> startLineMarkPositions;
    public List<MarkDTO> startLineMarks;
    public MarkDTO firstMark;
    public WindAndAdvantagesInfoForStartLineDTO startAnalysisWindLineInfoDTO;
    public List<StartAnalysisCompetitorDTO> startAnalysisCompetitorDTOs;
 
    
    public StartAnalysisDTO(){}

    @Override
    public int compareTo(StartAnalysisDTO o) {
        return raceName.compareTo(o.raceName);
    }
}