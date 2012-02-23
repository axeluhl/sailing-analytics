package com.sap.sailing.gwt.ui.shared;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;
import com.sap.sailing.domain.common.RaceIdentifier;

public class LeaderboardGroupDTO extends NamedDTO implements IsSerializable {

    public String description;
    public List<LeaderboardDTO> leaderboards;
    
    //Additional data
    private HashMap<RaceIdentifier, Date> racesStartDates;
    private HashMap<RaceIdentifier, PlacemarkOrderDTO> racesPlaces;
    
    /**
     * Creates a new LeaderboardGroupDTO with empty but non-null name, description and an empty but non-null list for the leaderboards.<br />
     * The additional data (start dates and places for the races) will be initialized but empty.
     */
    public LeaderboardGroupDTO() {
        this("", "", new ArrayList<LeaderboardDTO>());
    }

    /**
     * Creates a new LeaderboardGroupDTO with the given parameters as attributes.<br />
     * All parameters can be <code>null</code> but then the attributes will also be <code>null</code>.<br />
     * The additional data (start dates and places for the races) will be initialized but empty.
     */
    public LeaderboardGroupDTO(String name, String description, List<LeaderboardDTO> leaderboards) {
        super(name);
        this.description = description;
        this.leaderboards = leaderboards;
        this.racesStartDates = new HashMap<RaceIdentifier, Date>();
        this.racesPlaces = new HashMap<RaceIdentifier, PlacemarkOrderDTO>();
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
    
    /**
     * @return The start date of the given <code>race</code>, or <code>null</code> if no date for <code>race</code> is contained.
     */
    public Date getRaceStartDate(RaceIdentifier race) {
        return racesStartDates.get(race);
    }
    
    /**
     * Sets the <code>startDate</code> for the <code>race</code>. If a date for the race is already contained, the old date will be replaced.
     */
    public void setRaceStartDate(RaceIdentifier race, Date startDate) {
        racesStartDates.put(race, startDate);
    }
    
    /**
     * @return The {@link PlacemarkOrderDTO places} of the given <code>race</code>, or <code>null</code> if no places for <code>race</code> are contained.
     */
    public PlacemarkOrderDTO getRacePlaces(RaceIdentifier race) {
        return racesPlaces.get(race);
    }
    
    /**
     * Sets the <code>places</code> for the given <code>race</code>. If places for the race are already contained, the old places will be replaced.
     */
    public void setRacePlaces(RaceIdentifier race, PlacemarkOrderDTO places) {
        racesPlaces.put(race, places);
    }
    
    /**
     * 
     * @return The earliest date in the start dates of the races, or <code>null</code> if no start dates are contained
     */
    public Date getOverallStartDate() {
        Date start = null;
        if (!racesStartDates.isEmpty()) {
            start = new Date();
            for (Date date : racesStartDates.values()) {
                start = start.before(date) ? start : date;
            }
        }
        return start;
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
