package com.sap.sailing.gwt.ui.shared;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;
import com.sap.sailing.domain.common.RaceIdentifier;
import com.sap.sailing.domain.common.impl.Util.Pair;

public class LeaderboardGroupDTO extends NamedDTO implements IsSerializable {

    public String description;
    public List<LeaderboardDTO> leaderboards;
    
    //Additional data
    private HashMap<RaceIdentifier, Date> racesStartDates;
    private HashMap<LeaderboardDTO, Date> leaderboardsStartDates;
    
    private HashMap<RaceIdentifier, PlacemarkOrderDTO> racesPlaces;
    private HashMap<LeaderboardDTO, PlacemarkOrderDTO> leaderboardsPlaces;
    
    /**
     * Contains booleans to check if the data for a leaderboard needs to be calculated.<br />
     * A: leaderboard start date<br />
     * B: leaderboard places
     */
    private HashMap<LeaderboardDTO, Pair<Boolean, Boolean>> dataNeedsCalculation;
    private boolean dataNeedsCalculationNeedsInitialization;
    
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
        this.leaderboardsStartDates = new HashMap<LeaderboardDTO, Date>();
        this.racesPlaces = new HashMap<RaceIdentifier, PlacemarkOrderDTO>();
        this.leaderboardsPlaces = new HashMap<LeaderboardDTO, PlacemarkOrderDTO>();
        this.dataNeedsCalculation = new HashMap<LeaderboardDTO, Pair<Boolean,Boolean>>();
        this.dataNeedsCalculationNeedsInitialization = true;
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
     * Sets the <code>startDate</code> for the <code>race</code>. If a date for the race is already contained, the old date will be replaced.
     */
    public void setRaceStartDate(RaceIdentifier race, Date startDate) {
        racesStartDates.put(race, startDate);
        if (dataNeedsCalculationNeedsInitialization) {
            initializeDataNeedsCalculation();
        }
    }
    
    /**
     * @return The start date of the given <code>race</code>, or <code>null</code> if no date for <code>race</code> is contained.
     */
    public Date getRaceStartDate(RaceIdentifier race) {
        return racesStartDates.get(race);
    }
    
    /**
     * @return The earliest start date of the races in the given <code>leaderboard</code>, or <code>null</code> if
     *         <code>leaderboard</code> isn't contained or no start dates of the races are contained.
     */
    public Date getLeaderboardStartDate(LeaderboardDTO leaderboard) {
        Pair<Boolean, Boolean> dataNeedsCalculation = this.dataNeedsCalculation.get(leaderboard);
        if (dataNeedsCalculation != null && dataNeedsCalculation.getA()) {
            leaderboardsStartDates.put(leaderboard, calculateLeaderboardStartDate(leaderboard));
        }
        return leaderboardsStartDates.get(leaderboard);
    }

    private Date calculateLeaderboardStartDate(LeaderboardDTO leaderboard) {
        Date leaderboardStart = null;
        if (leaderboards.contains(leaderboard)) {
            for (RaceInLeaderboardDTO race : leaderboard.getRaceList()) {
                if (race.isTrackedRace()) {
                    Date raceStart = racesStartDates.get(race.getRaceIdentifier());
                    if (raceStart != null) {
                        if (leaderboardStart == null) {
                            leaderboardStart = new Date();
                        }
                        leaderboardStart = leaderboardStart.before(raceStart) ? leaderboardStart : raceStart;
                    }
                }
            }
            dataNeedsCalculation.get(leaderboard).setA(false);
            dataNeedsCalculationNeedsInitialization = true;
        }
        return leaderboardStart;
    }
    
    /**
     * 
     * @return The earliest date in the start dates of the races, or <code>null</code> if no start dates are contained
     */
    public Date getGroupStartDate() {
        Date groupStart = null;
        for (LeaderboardDTO leaderboard : leaderboards) {
            Date leaderboardStart = getLeaderboardStartDate(leaderboard);
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
     * Sets the <code>places</code> for the given <code>race</code>. If places for the race are already contained, the old places will be replaced.
     */
    public void setRacePlaces(RaceIdentifier race, PlacemarkOrderDTO places) {
        racesPlaces.put(race, places);
        if (dataNeedsCalculationNeedsInitialization) {
            initializeDataNeedsCalculation();
        }
    }
    
    /**
     * @return The {@link PlacemarkOrderDTO places} of the given <code>race</code>, or <code>null</code> if no places for <code>race</code> are contained.
     */
    public PlacemarkOrderDTO getRacePlaces(RaceIdentifier race) {
        return racesPlaces.get(race);
    }
    
    /**
     * Takes the {@link PlacemarkOrderDTO} of all races in the {@link LeaderboardDTO} <code>leaderboard</code>, if the
     * PlacemarkOrderDTO for the race is contained, and fills all {@link PlacemarkDTO} in a new PlacemarkOrderDTO.<br />
     * The order of the races in the leaderboard determine the order of the PlacemarkDTOs in the PlacemarkOrderDTO.
     * 
     * @return The places of <code>leaderboard</code> in form of a {@link PlacemarkOrderDTO}, or <code>null</code> if
     *         <code>leaderboard</code> isn't contained or the {@link PlacemarkOrderDTO places} of no race in
     *         <code>leaderboard</code> are contained
     */
    public PlacemarkOrderDTO getLeaderboardPlaces(LeaderboardDTO leaderboard) {
        Pair<Boolean, Boolean> dataNeedsCalculation = this.dataNeedsCalculation.get(leaderboard);
        if (dataNeedsCalculation != null && dataNeedsCalculation.getB()) {
            leaderboardsPlaces.put(leaderboard, calculateLeaderboardPlaces(leaderboard));
        }
        return leaderboardsPlaces.get(leaderboard);
    }

    private PlacemarkOrderDTO calculateLeaderboardPlaces(LeaderboardDTO leaderboard) {
        PlacemarkOrderDTO leaderboardPlaces = null;
        if (leaderboards.contains(leaderboard)) {
            for (RaceInLeaderboardDTO race : leaderboard.getRaceList()) {
                if (race.isTrackedRace()) {
                    PlacemarkOrderDTO racePlaces = racesPlaces.get(race.getRaceIdentifier());
                    if (racePlaces != null) {
                        if (leaderboardPlaces == null) {
                            leaderboardPlaces = new PlacemarkOrderDTO();
                        }
                        leaderboardPlaces.getPlacemarks().addAll(racePlaces.getPlacemarks());
                    }
                }
            }
            dataNeedsCalculation.get(leaderboard).setB(false);
            dataNeedsCalculationNeedsInitialization = true;
        }
        return leaderboardPlaces;
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
        for (LeaderboardDTO leaderboard : leaderboards) {
            PlacemarkOrderDTO leaderboardPlaces = getLeaderboardPlaces(leaderboard);
            if (leaderboardPlaces != null) {
                places.add(leaderboardPlaces);
            }
        }
        return places;
    }
    
    private void initializeDataNeedsCalculation() {
        for (LeaderboardDTO leaderboard : leaderboards) {
            dataNeedsCalculation.put(leaderboard, new Pair<Boolean, Boolean>(true, true));
        }
        dataNeedsCalculationNeedsInitialization = false;
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
