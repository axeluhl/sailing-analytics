package com.sap.sailing.gwt.home.communication.event.minileaderboard;

import com.sap.sailing.gwt.dispatch.client.DTO;
import com.sap.sailing.gwt.home.communication.event.SimpleCompetitorDTO;

public class MiniLeaderboardItemDTO implements DTO {
    private SimpleCompetitorDTO competitor;
    private int rank;
    private Double points;
    private int raceCount;
    
    @SuppressWarnings("unused")
    private MiniLeaderboardItemDTO() {
    }
    
    public MiniLeaderboardItemDTO(SimpleCompetitorDTO competitor, int rank, Double points, int raceCount) {
        super();
        this.competitor = competitor;
        this.rank = rank;
        this.points = points;
        this.raceCount = raceCount;
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
    
    public int getRaceCount() {
        return raceCount;
    }
}