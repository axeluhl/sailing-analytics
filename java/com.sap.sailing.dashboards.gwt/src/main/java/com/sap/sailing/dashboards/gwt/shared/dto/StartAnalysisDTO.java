package com.sap.sailing.dashboards.gwt.shared.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.domain.common.dto.CompetitorDTO;
import com.sap.sailing.domain.common.racelog.RacingProcedureType;

public class StartAnalysisDTO implements Serializable, Comparable<StartAnalysisDTO> {

    private static final long serialVersionUID = -1325150193180234561L;

    public String raceName;
    public CompetitorDTO competitor;
    public WindAndAdvantagesInfoForStartLineDTO startAnalysisWindLineInfoDTO;
    public List<StartAnalysisCompetitorDTO> startAnalysisCompetitorDTOs;
    public long timeOfStartInMilliSeconds;
    public RegattaAndRaceIdentifier regattaAndRaceIdentifier;
    public RacingProcedureType racingProcedureType;
    public long tailLenghtInMilliseconds;

    public StartAnalysisDTO() {
    }

    @Override
    public int compareTo(StartAnalysisDTO o) {
        return raceName.compareTo(o.raceName);
    }

    public List<CompetitorDTO> getCompetitorDTOsFromStartAnaylsisCompetitorDTOs() {
        List<CompetitorDTO> competitorDTOs = new ArrayList<>();
        for (StartAnalysisCompetitorDTO startAnalysisCompetitorDTO : startAnalysisCompetitorDTOs) {
            competitorDTOs.add(startAnalysisCompetitorDTO.competitorDTO);
        }
        return competitorDTOs;
    }
}