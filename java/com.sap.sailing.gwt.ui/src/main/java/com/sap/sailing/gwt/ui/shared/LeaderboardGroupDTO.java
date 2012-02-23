package com.sap.sailing.gwt.ui.shared;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;
import com.sap.sailing.domain.common.RaceIdentifier;

public class LeaderboardGroupDTO extends NamedDTO implements IsSerializable {

    public String description;
    public List<LeaderboardDTO> leaderboards;
    
    /**
     * Creates a new LeaderboardGroupDTO with empty but non-null name, description and an empty but non-null list for the leaderboards.
     */
    public LeaderboardGroupDTO() {
        this.name = "";
        this.description = "";
        this.leaderboards = new ArrayList<LeaderboardDTO>();
    }

    /**
     * Creates a new LeaderboardGroupDTO with the given parameters as attributes.<br />
     * All parameters can be <code>null</code> but then the attributes will also be <code>null</code>.
     */
    public LeaderboardGroupDTO(String name, String description, List<LeaderboardDTO> leaderboards) {
        super(name);
        this.description = description;
        this.leaderboards = leaderboards;
    }
    
    public boolean containsRace(RaceIdentifier race) {
        boolean containsRace = false;
        leaderboardsLoop:
        for (LeaderboardDTO leaderboard : leaderboards) {
            for (RaceInLeaderboardDTO raceInLeaderboard : leaderboard.getRaceList()) {
                if (raceInLeaderboard.getRaceIdentifier() != null && raceInLeaderboard.getRaceIdentifier().equals(race)) {
                    containsRace = true;
                    break leaderboardsLoop;
                }
            }
        }
        return containsRace;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((description == null) ? 0 : description.hashCode());
        result = prime * result + ((leaderboards == null) ? 0 : leaderboards.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        LeaderboardGroupDTO other = (LeaderboardGroupDTO) obj;
        if (description == null) {
            if (other.description != null)
                return false;
        } else if (!description.equals(other.description))
            return false;
        if (leaderboards == null) {
            if (other.leaderboards != null)
                return false;
        } else if (!leaderboards.equals(other.leaderboards))
            return false;
        return true;
    }
    
}
