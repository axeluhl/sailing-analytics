package com.sap.sailing.gwt.ui.shared;

import java.util.Map;

import com.google.gwt.user.client.rpc.IsSerializable;
import com.sap.sailing.domain.common.dto.BoatDTO;
import com.sap.sailing.domain.common.dto.CompetitorDTO;

public class RaceboardDataDTO implements IsSerializable {
    private RaceWithCompetitorsAndBoatsDTO race;
    private boolean isValidLeaderboardGroup;
    private boolean isValidLeaderboard;
    private boolean isValidEvent;
    
    // for GWT
    RaceboardDataDTO() {}
    
    public RaceboardDataDTO(RaceWithCompetitorsAndBoatsDTO race, boolean isValidLeaderboard, boolean isValidLeaderboardGroup, boolean isValidEvent) {
        this.race = race;
        this.isValidLeaderboard = isValidLeaderboard;
        this.isValidLeaderboardGroup = isValidLeaderboardGroup;
        this.isValidEvent = isValidEvent;
    }

    public Map<CompetitorDTO, BoatDTO> getCompetitorAndTheirBoats() {
        return race.getCompetitorsAndBoats();
    }

    public Iterable<CompetitorDTO> getCompetitors() {
        return race.getCompetitors();
    }

    public RaceWithCompetitorsAndBoatsDTO getRace() {
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
