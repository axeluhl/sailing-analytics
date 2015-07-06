package com.sap.sailing.gwt.ui.shared.dispatch.event;

import com.sap.sailing.gwt.ui.shared.dispatch.DTO;

public class MiniLeaderboardItemDTO implements DTO {
    private SimpleCompetitorDTO competitor;
    private int rank;
    private Double points;
    
    @SuppressWarnings("unused")
    private MiniLeaderboardItemDTO() {
    }
    
    public MiniLeaderboardItemDTO(SimpleCompetitorDTO competitor, int rank, Double points) {
        super();
        this.competitor = competitor;
        this.rank = rank;
        this.points = points;
    }
    
    public SimpleCompetitorDTO getCompetitor() {
        return competitor;
    }
    
    public Double getPoints() {
        return points;
    }
    
    public int getRank() {
        return rank;
    }
}