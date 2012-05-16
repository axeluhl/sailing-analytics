package com.sap.sailing.gwt.ui.shared;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gwt.user.client.rpc.IsSerializable;
import com.sap.sailing.domain.common.RaceIdentifier;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;

public class RaceColumnDTO implements IsSerializable {
    private String raceColumnName;
    private boolean medalRace;
    private List<String> fleetNames;
    private Map<String, RegattaAndRaceIdentifier> trackedRaceIdentifiersPerFleet;
    private Map<String, StrippedRaceDTO> racesPerFleet;

    public RaceColumnDTO() {
        trackedRaceIdentifiersPerFleet = new HashMap<String, RegattaAndRaceIdentifier>();
        racesPerFleet = new HashMap<String, StrippedRaceDTO>();
        fleetNames = new ArrayList<String>();
    }
    
    public String getRaceColumnName() {
        return raceColumnName;
    }

    public void setRaceColumnName(String raceColumnName) {
        this.raceColumnName = raceColumnName;
    }

    public boolean isMedalRace() {
        return medalRace;
    }

    public void setMedalRace(boolean medalRace) {
        this.medalRace = medalRace;
    }

    public boolean isTrackedRace(String fleetName) {
        return trackedRaceIdentifiersPerFleet.get(fleetName) != null;
    }

    public void setRaceIdentifier(String fleetName, RegattaAndRaceIdentifier raceIdentifier) {
        this.trackedRaceIdentifiersPerFleet.put(fleetName, raceIdentifier);
    }

    /**
     * @return a non-<code>null</code> race identifier if this column represents a <em>tracked</em> race. Such a race's
     *         data can be obtained from the server in great detail, as opposed to non-tracked races for which only
     *         result points may have been entered manually.
     */
    public RegattaAndRaceIdentifier getRaceIdentifier(String fleetName) {
        return trackedRaceIdentifiersPerFleet.get(fleetName);
    }

    /**
     * Returns an object with data (e.g. start date or places) for the RaceInLeaderboardDTO. Is <code>null</code>, if
     * the method {@link RaceColumnDTO#isTrackedRace(String)} returns <code>false</code>.
     * 
     * @return An Object with additional data, or <code>null</code> if the race isn't tracked
     */
    public StrippedRaceDTO getRace(String fleetName) {
        return racesPerFleet.get(fleetName);
    }

    public void setRace(String fleetName, StrippedRaceDTO race) {
        this.racesPerFleet.put(fleetName, race);
    }
    
    public Iterable<String> getFleetNames() {
        return fleetNames;
    }
    
    /**
     * @return The start of race, or the start of tracking if the start of race is <code>null</code>, or
     *         <code>null</code> if no start date is available.
     */
    public Date getStartDate(String fleetName) {
        Date start = null;
        if (racesPerFleet.get(fleetName) != null) {
            start = racesPerFleet.get(fleetName).getStartDate();
        }
        return start;
    }
    
    /**
     * @return The {@link PlacemarkOrderDTO places} or <code>null</code>, if no places are available
     */
    public PlacemarkOrderDTO getPlaces() {
        PlacemarkOrderDTO places = null;
        for (StrippedRaceDTO race : racesPerFleet.values()) {
            if (places == null) {
                places = race.places;
            } else {
                places.add(race.places);
            }
        }
        return places;
    }
    
    /**
     * @return <code>true</code> if the startOfTracking is after the current date and there's no end of the race
     */
    public boolean isLive() {
        for (String fleetName : getFleetNames()) {
            final StrippedRaceDTO strippedRaceDTO = racesPerFleet.get(fleetName);
            if (trackedRaceIdentifiersPerFleet.get(fleetName) != null
                    && strippedRaceDTO != null
                    && strippedRaceDTO.endOfRace == null
                    && (strippedRaceDTO.startOfTracking != null ? new Date().after(strippedRaceDTO.startOfTracking) : false)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((fleetNames == null) ? 0 : fleetNames.hashCode());
        result = prime * result + (medalRace ? 1231 : 1237);
        result = prime * result + ((raceColumnName == null) ? 0 : raceColumnName.hashCode());
        result = prime * result + ((racesPerFleet == null) ? 0 : racesPerFleet.hashCode());
        result = prime * result
                + ((trackedRaceIdentifiersPerFleet == null) ? 0 : trackedRaceIdentifiersPerFleet.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        RaceColumnDTO other = (RaceColumnDTO) obj;
        if (fleetNames == null) {
            if (other.fleetNames != null)
                return false;
        } else if (!fleetNames.equals(other.fleetNames))
            return false;
        if (medalRace != other.medalRace)
            return false;
        if (raceColumnName == null) {
            if (other.raceColumnName != null)
                return false;
        } else if (!raceColumnName.equals(other.raceColumnName))
            return false;
        if (racesPerFleet == null) {
            if (other.racesPerFleet != null)
                return false;
        } else if (!racesPerFleet.equals(other.racesPerFleet))
            return false;
        if (trackedRaceIdentifiersPerFleet == null) {
            if (other.trackedRaceIdentifiersPerFleet != null)
                return false;
        } else if (!trackedRaceIdentifiersPerFleet.equals(other.trackedRaceIdentifiersPerFleet))
            return false;
        return true;
    }

    public boolean containsRace(RaceIdentifier preSelectedRace) {
        return trackedRaceIdentifiersPerFleet.values().contains(preSelectedRace);
    }

    public boolean hasTrackedRaces() {
        Set<RegattaAndRaceIdentifier> raceIdentifiers = new HashSet<RegattaAndRaceIdentifier>(trackedRaceIdentifiersPerFleet.values());
        raceIdentifiers.remove(null);
        return !raceIdentifiers.isEmpty();
    }

    public void addFleetName(String fleetName) {
        fleetNames.add(fleetName);
    }

}
