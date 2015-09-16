package com.sap.sailing.gwt.ui.shared;

import java.util.List;
import java.util.Map;

import com.google.gwt.user.client.rpc.IsSerializable;
import com.sap.sailing.domain.common.dto.BoatDTO;
import com.sap.sailing.domain.common.dto.CompetitorDTO;

public class RaceboardDataDTO implements IsSerializable {
    private Map<CompetitorDTO, BoatDTO> competitorAndTheirBoats;
    private RaceWithCompetitorsDTO race;
    private boolean isValidLeaderboardGroup;
    private boolean isValidLeaderboard;
    private boolean isValidEvent;
    
    // for GWT
    RaceboardDataDTO() {}
    
    public RaceboardDataDTO(RaceWithCompetitorsDTO race, Map<CompetitorDTO, BoatDTO> competitorAndTheirBoats, 
            boolean isValidLeaderboard, boolean isValidLeaderboardGroup, boolean isValidEvent) {
        this.race = race;
        this.competitorAndTheirBoats = competitorAndTheirBoats;
        this.isValidLeaderboard = isValidLeaderboard;
        this.isValidLeaderboardGroup = isValidLeaderboardGroup;
        this.isValidEvent = isValidEvent;
    }

    public Map<CompetitorDTO, BoatDTO> getCompetitorAndTheirBoats() {
        return competitorAndTheirBoats;
    }

    public List<CompetitorDTO> getCompetitors() {
        return race.getCompetitors();
    }

    public RaceWithCompetitorsDTO getRace() {
        return race;
    }

    public boolean isValidLeaderboardGroup() {
        return isValidLeaderboardGroup;
    }

    public boolean isValidLeaderboard() {
        return isValidLeaderboard;
    }

    public boolean isValidEvent() {
        return isValidEvent;
    }
}
