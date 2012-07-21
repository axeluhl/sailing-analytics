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

public class RaceColumnDTO extends NamedDTO implements IsSerializable {
    private boolean medalRace;
    private List<FleetDTO> fleets;
    private Map<FleetDTO, RegattaAndRaceIdentifier> trackedRaceIdentifiersPerFleet;
    private Map<FleetDTO, StrippedRaceDTO> racesPerFleet;
    private Map<FleetDTO, Long> delayToLiveInMillisPerFleet;

    public RaceColumnDTO() {
        trackedRaceIdentifiersPerFleet = new HashMap<FleetDTO, RegattaAndRaceIdentifier>();
        racesPerFleet = new HashMap<FleetDTO, StrippedRaceDTO>();
        fleets = new ArrayList<FleetDTO>();
        delayToLiveInMillisPerFleet = new HashMap<FleetDTO, Long>();
    }
    
    public String getRaceColumnName() {
        return name;
    }
    
    public boolean hasTrackedRace(RaceIdentifier raceIdentifier) {
        return trackedRaceIdentifiersPerFleet.values().contains(raceIdentifier);
    }
    
    public boolean isMedalRace() {
        return medalRace;
    }

    public void setMedalRace(boolean medalRace) {
        this.medalRace = medalRace;
    }

    public boolean isTrackedRace(FleetDTO fleet) {
        return trackedRaceIdentifiersPerFleet.get(fleet) != null;
    }

    public void setRaceIdentifier(FleetDTO fleet, RegattaAndRaceIdentifier raceIdentifier) {
        this.trackedRaceIdentifiersPerFleet.put(fleet, raceIdentifier);
    }

    /**
     * @return a non-<code>null</code> race identifier if this column represents a <em>tracked</em> race. Such a race's
     *         data can be obtained from the server in great detail, as opposed to non-tracked races for which only
     *         result points may have been entered manually.
     */
    public RegattaAndRaceIdentifier getRaceIdentifier(FleetDTO fleet) {
        return trackedRaceIdentifiersPerFleet.get(fleet);
    }

    /**
     * Returns an object with data (e.g. start date or places) for the RaceInLeaderboardDTO. Is <code>null</code>, if
     * the method {@link RaceColumnDTO#isTrackedRace(String)} returns <code>false</code>.
     * 
     * @return An Object with additional data, or <code>null</code> if the race isn't tracked
     */
    public StrippedRaceDTO getRace(FleetDTO fleet) {
        return racesPerFleet.get(fleet);
    }

    public void setRace(FleetDTO fleet, StrippedRaceDTO race) {
        this.racesPerFleet.put(fleet, race);
    }
    
    public Iterable<FleetDTO> getFleets() {
        return fleets;
    }
    
    /**
     * @return The start of race, or the start of tracking if the start of race is <code>null</code>, or
     *         <code>null</code> if no start date is available.
     */
    public Date getStartDate(FleetDTO fleet) {
        Date start = null;
        if (racesPerFleet.get(fleet) != null) {
            start = racesPerFleet.get(fleet).getStartDate();
        }
        return start;
    }
    
    /**
     * @return The {@link PlacemarkOrderDTO places} or <code>null</code>, if no places are available
     */
    public PlacemarkOrderDTO getPlaces() {
        PlacemarkOrderDTO places = null;
        for (StrippedRaceDTO race : racesPerFleet.values()) {
            if (race != null) {
                if (places == null) {
                    places = race.places;
                } else {
                    places.add(race.places);
                }
            }
        }
        return places;
    }
    
    /**
     * @return <code>true</code> if the startOfTracking is after the current date and there's no end of the race
     */
    public boolean isLive() {
        for (FleetDTO fleet : getFleets()) {
            final StrippedRaceDTO strippedRaceDTO = racesPerFleet.get(fleet);
            if (trackedRaceIdentifiersPerFleet.get(fleet) != null
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
        result = prime * result + ((fleets == null) ? 0 : fleets.hashCode());
        result = prime * result + (medalRace ? 1231 : 1237);
        result = prime * result + ((name == null) ? 0 : name.hashCode());
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
        if (fleets == null) {
            if (other.fleets != null)
                return false;
        } else if (!fleets.equals(other.fleets))
            return false;
        if (medalRace != other.medalRace)
            return false;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
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

    public void addFleet(FleetDTO fleet) {
        fleets.add(fleet);
    }

    public long getDelayToLiveInMillis(FleetDTO fleet) {
        return delayToLiveInMillisPerFleet.get(fleet);
    }

    public void setDelayToLiveInMillis(FleetDTO fleet, long delayToLiveInMillis) {
        this.delayToLiveInMillisPerFleet.put(fleet, delayToLiveInMillis);
    }
}
