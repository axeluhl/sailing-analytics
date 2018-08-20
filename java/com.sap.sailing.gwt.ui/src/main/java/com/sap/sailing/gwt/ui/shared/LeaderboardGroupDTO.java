package com.sap.sailing.gwt.ui.shared;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import com.sap.sailing.domain.common.RaceIdentifier;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.domain.common.ScoringSchemeType;
import com.sap.sailing.domain.common.dto.FleetDTO;
import com.sap.sailing.domain.common.dto.PlacemarkDTO;
import com.sap.sailing.domain.common.dto.PlacemarkOrderDTO;
import com.sap.sailing.domain.common.dto.RaceColumnDTO;
import com.sap.sse.gwt.client.player.Timer;

public class LeaderboardGroupDTO extends LeaderboardGroupBaseDTO {
    private static final long serialVersionUID = -2923229069598593687L;
    public List<StrippedLeaderboardDTO> leaderboards;
    public boolean displayLeaderboardsInReverseOrder;
    
    private int[] overallLeaderboardDiscardThresholds;
    private ScoringSchemeType overallLeaderboardScoringSchemeType;

    /**
     * The current time on the server when this object was created. Clients can use this to synchronize a clock
     * difference, e.g., in the {@link Timer} class.
     */
    private Date currentServerTime;
    
    LeaderboardGroupDTO() {}
    
    /**
     * Creates a new LeaderboardGroupDTO with empty but non-null name, description and an empty but non-null list for
     * the leaderboards. The {@link #currentServerTime} will be set to the creation time on the system that creates this
     * object.
     * <p>
     * The additional data (start dates and places for the races) will be initialized but empty.
     */
    public LeaderboardGroupDTO(UUID id, String displayName) {
        this(id, "", displayName);
    }
    
    public LeaderboardGroupDTO(UUID id, String name, String displayName) {
        this(id, name, displayName, "");
    }

    public LeaderboardGroupDTO(UUID id, String name, String displayName, String description) {
        this(id, name, description, displayName, new ArrayList<StrippedLeaderboardDTO>());
    }

    /**
     * Creates a new LeaderboardGroupDTO with the given parameters as attributes.<br />
     * All parameters can be <code>null</code> but then the attributes will also be <code>null</code>.<br />
     * The additional data (start dates and places for the races) will be initialized but empty.
     * @param displayName TODO
     */
    private LeaderboardGroupDTO(UUID id, String name, String description, String displayName, List<StrippedLeaderboardDTO> leaderboards) {
        super(id, name, displayName);
        currentServerTime = new Date();
        this.description = description;
        this.leaderboards = leaderboards;
    }
    
    public boolean hasOverallLeaderboard() {
        return getOverallLeaderboardScoringSchemeType() != null;
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
    
    public List<StrippedLeaderboardDTO> getLeaderboardsInReverseOrder() {
        List<StrippedLeaderboardDTO> leaderboardsInReverseOrder = new ArrayList<StrippedLeaderboardDTO>(leaderboards);
        Collections.reverse(leaderboardsInReverseOrder);
        return Collections.unmodifiableList(leaderboardsInReverseOrder);
    }
    
    public boolean containsRegattaLeaderboard() {
        boolean result = false;
        for (StrippedLeaderboardDTO leaderboard : leaderboards) {
            if (leaderboard.type.isRegattaLeaderboard()) {
                result = true;
                break;
            }
        }
        return result;
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
    public boolean hasLiveRace(long serverTimePointAsMillis) {
        for (StrippedLeaderboardDTO leaderboard : leaderboards) {
            if (leaderboard.hasLiveRace(serverTimePointAsMillis)) {
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

    public List<StrippedLeaderboardDTO> getLeaderboards() {
        return leaderboards;
    }

    public Long getAverageDelayToLiveInMillis() {
        Long result = null;
        long delaySum = 0;
        long count = 0;
        for (StrippedLeaderboardDTO leaderboard : leaderboards) {
            if (leaderboard.getDelayToLiveInMillisForLatestRace() != null) {
                delaySum += leaderboard.getDelayToLiveInMillisForLatestRace();
                count++;
            }
        }
        if (count > 0) {
            result = delaySum / count;
        }
        return result;
    }

    public Date getCurrentServerTime() {
        return currentServerTime;
    }

}
