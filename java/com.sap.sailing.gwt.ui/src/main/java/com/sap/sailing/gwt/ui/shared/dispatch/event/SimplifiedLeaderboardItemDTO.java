package com.sap.sailing.gwt.ui.shared.dispatch.event;

import com.sap.sailing.domain.common.dto.CompetitorDTO;
import com.sap.sailing.gwt.ui.shared.dispatch.DTO;

public class SimplifiedLeaderboardItemDTO implements DTO {
    private CompetitorDTO competitor;
    private int rank;
    private Double points;
    
    @SuppressWarnings("unused")
    private SimplifiedLeaderboardItemDTO() {
    }
    
    public SimplifiedLeaderboardItemDTO(CompetitorDTO competitor, int rank, Double points) {
        super();
        this.competitor = competitor;
        this.rank = rank;
        this.points = points;
    }
    
    public CompetitorDTO getCompetitor() {
        return competitor;
    }
    
    public Double getPoints() {
        return points;
    }
    
    public int getRank() {
        return rank;
    }
}