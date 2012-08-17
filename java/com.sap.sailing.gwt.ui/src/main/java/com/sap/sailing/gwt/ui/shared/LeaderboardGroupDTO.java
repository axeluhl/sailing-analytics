package com.sap.sailing.gwt.ui.shared;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;
import com.sap.sailing.domain.common.RaceIdentifier;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.domain.common.ScoringSchemeType;

public class LeaderboardGroupDTO extends NamedDTO implements IsSerializable {

    public String description;
    public List<StrippedLeaderboardDTO> leaderboards;
    
    private int[] overallLeaderboardDiscardThresholds;
    private ScoringSchemeType overallLeaderboardScoringSchemeType;
    
    /**
     * Creates a new LeaderboardGroupDTO with empty but non-null name, description and an empty but non-null list for the leaderboards.<br />
     * The additional data (start dates and places for the races) will be initialized but empty.
     */
    public LeaderboardGroupDTO() {
        this("", "", new ArrayList<StrippedLeaderboardDTO>());
    }

    /**
     * Creates a new LeaderboardGroupDTO with the given parameters as attributes.<br />
     * All parameters can be <code>null</code> but then the attributes will also be <code>null</code>.<br />
     * The additional data (start dates and places for the races) will be initialized but empty.
     */
    public LeaderboardGroupDTO(String name, String description, List<StrippedLeaderboardDTO> leaderboards) {
        super(name);
        this.description = description;
        this.leaderboards = leaderboards;
    }
    
    public boolean hasOverallLeaderboard() {
        return getOverallLeaderboardDiscardThresholds() != null;
    }
    
    public int[] getOverallLeaderboardDiscardThresholds() {
        return overallLeaderboardDiscardThresholds;
    }

    public void setOverallLeaderboardDiscardThresholds(int[] overallLeaderboardDiscardThresholds) {
        this.overallLeaderboardDiscardThresholds = overallLeaderboardDiscardThresholds;
    }

    public ScoringSchemeType getOverallLeaderboardScoringSchemeType() {
        return overallLeaderboardScoringSchemeType;
    }

    public void setOverallLeaderboardScoringSchemeType(ScoringSchemeType overallLeaderboardScoringSchemeType) {
        this.overallLeaderboardScoringSchemeType = overallLeaderboardScoringSchemeType;
    }

    public boolean containsRace(RaceIdentifier race) {
        boolean containsRace = false;
        leaderboardsLoop:
        for (StrippedLeaderboardDTO leaderboard : leaderboards) {
            for (RaceColumnDTO raceInLeaderboard : leaderboard.getRaceList()) {
                for (FleetDTO fleet : raceInLeaderboard.getFleets()) {
                    final RegattaAndRaceIdentifier raceIdentifierForFleet = raceInLeaderboard.getRaceIdentifier(fleet);
                    if (raceIdentifierForFleet != null && raceIdentifierForFleet.equals(race)) {
                        containsRace = true;
                        break leaderboardsLoop;
                    }
                }
            }
        }
        return containsRace;
    }
    
    /**
     * @return The earliest date in the start dates of the leaderboards, or <code>null</code> if no start dates are contained
     */
    public Date getGroupStartDate() {
        Date groupStart = null;
        for (StrippedLeaderboardDTO leaderboard : leaderboards) {
            Date leaderboardStart = leaderboard.getStartDate();
            if (leaderboardStart != null) {
                if (groupStart == null) {
                    groupStart = new Date();
                }
                groupStart = groupStart.before(leaderboardStart) ? groupStart : leaderboardStart;
            }
        }
        return groupStart;
    }
    
    /**
     * Uses {@link LeaderboardGroupDTO#getLeaderboardPlaces(leaderboard) LeaderboardGroupDTO.getLeaderboardPlaces} to
     * create the {@link PlacemarkOrderDTO places} for all contained leaderboards and returns them as a list.
     * 
     * @return A list of the {@link PlacemarkDTO places} of all contained leaderboards.<br />
     *         The returning list is never <code>null</code>, but can be empty.
     */
    public List<PlacemarkOrderDTO> getGroupPlaces() {
        List<PlacemarkOrderDTO> places = new ArrayList<PlacemarkOrderDTO>();
        for (StrippedLeaderboardDTO leaderboard : leaderboards) {
            PlacemarkOrderDTO leaderboardPlaces = leaderboard.getPlaces();
            if (leaderboardPlaces != null) {
                places.add(leaderboardPlaces);
            }
        }
        return places;
    }
    
    /**
     * @return <code>true</code> if the group contains a race which is live.
     */
    public boolean containsLiveRace() {
        for (StrippedLeaderboardDTO leaderboard : leaderboards) {
            if (leaderboard.containsLiveRace()) {
                return true;
            }
        }
        return false;
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
