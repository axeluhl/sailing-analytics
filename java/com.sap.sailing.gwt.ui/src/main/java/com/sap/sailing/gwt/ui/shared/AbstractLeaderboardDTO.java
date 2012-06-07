package com.sap.sailing.gwt.ui.shared;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.google.gwt.user.client.rpc.IsSerializable;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.domain.common.impl.Util;

public abstract class AbstractLeaderboardDTO implements IsSerializable {
    public String name;
    
    private List<RaceColumnDTO> races;
    public Map<CompetitorDTO, String> competitorDisplayNames;
    public Map<CompetitorDTO, LeaderboardRowDTO> rows;
    public boolean hasCarriedPoints;
    public int[] discardThresholds;

    public AbstractLeaderboardDTO() {
        races = new ArrayList<RaceColumnDTO>();
    }

    public String getDisplayName(CompetitorDTO competitor) {
        if (competitorDisplayNames == null || competitorDisplayNames.get(competitor) == null) {
            return competitor.name;
        } else {
            return competitorDisplayNames.get(competitor);
        }
    }

    /**
     * If the race whose name is specified in <code>raceName</code> has any competitor who has valid
     * {@link LeaderboardEntryDTO#legDetails} for that race, the number of entries in the leg details is returned,
     * telling the number of legs that the race has. Otherwise, -1 is returned.
     */
    public int getLegCount(String raceName) {
        for (LeaderboardRowDTO row : rows.values()) {
            if (row.fieldsByRaceColumnName.get(raceName) != null && row.fieldsByRaceColumnName.get(raceName).legDetails != null) {
                return row.fieldsByRaceColumnName.get(raceName).legDetails.size();
            }
        }
        return -1;
    }

    /**
     * Tells if the <code>competitor</code> scored (and therefore presumably participated) in a medal race represented
     * in this leaderboard.
     */
    public boolean scoredInMedalRace(CompetitorDTO competitor) {
        LeaderboardRowDTO row = rows.get(competitor);
        for (RaceColumnDTO race : races) {
            if (race.isMedalRace() && row.fieldsByRaceColumnName.get(race.getRaceColumnName()).totalPoints > 0) {
                return true;
            }
        }
        return false;
    }

    public int getTotalPoints(LeaderboardRowDTO object) {
        int totalPoints = object.carriedPoints == null ? 0 : object.carriedPoints;
        for (LeaderboardEntryDTO e : object.fieldsByRaceColumnName.values()) {
            totalPoints += e.totalPoints;
        }
        return totalPoints;
    }

    public int getNetPoints(CompetitorDTO competitor, String nameOfLastRaceSoFar) {
        int result = 0;
        LeaderboardRowDTO row = rows.get(competitor);
        if (row != null) {
            LeaderboardEntryDTO field = row.fieldsByRaceColumnName.get(nameOfLastRaceSoFar);
            if (field != null) {
                result = field.netPoints;
            }
        }
        return result;
    }

    public boolean raceIsTracked(String raceColumnName) {
        for (RaceColumnDTO race : races) {
            if (race.getRaceColumnName().equals(raceColumnName)) {
                for (String fleetName : race.getFleetNames()) {
                    if (race.isTrackedRace(fleetName)) {
                        return true;
                    }
                }
            }
        }
        return false;

    }

    public boolean raceIsMedalRace(String raceColumnName) {
        return getRaceInLeaderboardByName(raceColumnName).isMedalRace();
    }

    private RaceColumnDTO getOrCreateRaceColumn(String raceColumnName) {
        RaceColumnDTO result = getRaceInLeaderboardByName(raceColumnName);
        if (result == null) {
            result = new RaceColumnDTO();
            result.setRaceColumnName(raceColumnName);
            races.add(result);
        }
        return result;
    }
    
    /**
     * If the {@link RaceColumnDTO} by the name <code>raceColumnName</code> doesn't exist yet within this leaderboard
     * DTO, it is created. This method ensures that a fleet named <code>fleetName</code> is present. If it's not present
     * yet, it's added to the race column's fleet name list. The <code>trackedRaceIdentifier</code> and
     * <code>race</code> are associated with the column for the fleet identified by <code>fleetName</code>.
     * 
     * @param fleetName
     *            must not be null
     */
    public void addRace(String raceColumnName, String fleetName, boolean medalRace,
            RegattaAndRaceIdentifier trackedRaceIdentifier, StrippedRaceDTO race) {
        assert fleetName != null;
        RaceColumnDTO raceInLeaderboardDTO = getOrCreateRaceColumn(raceColumnName);
        if (!Util.contains(raceInLeaderboardDTO.getFleetNames(), fleetName)) {
            raceInLeaderboardDTO.addFleetName(fleetName);
        }
        raceInLeaderboardDTO.setMedalRace(medalRace);
        raceInLeaderboardDTO.setRaceIdentifier(fleetName, trackedRaceIdentifier);
        raceInLeaderboardDTO.setRace(fleetName, race);
    }

    /**
     * A new {@link RaceColumnDTO} by the name <code>raceColumnName</code> is created and added to this leaderboard at
     * position <code>index</code>. The single fleet named <code>fleetName</code> is added to the column. The
     * <code>trackedRaceIdentifier</code> and <code>race</code> are associated with the column for the fleet identified
     * by <code>fleetName</code>.
     * 
     * @param fleetName
     *            must not be null
     */
    public void createRaceColumnAt(String raceColumnName, String fleetName, boolean medalRace,
            RegattaAndRaceIdentifier trackedRaceIdentifier, int index) {
        assert fleetName != null;
        RaceColumnDTO raceInLeaderboardDTO = new RaceColumnDTO();
        raceInLeaderboardDTO.setRaceColumnName(raceColumnName);
        raceInLeaderboardDTO.setMedalRace(medalRace);
        raceInLeaderboardDTO.addFleetName(fleetName);
        raceInLeaderboardDTO.setRaceIdentifier(fleetName, trackedRaceIdentifier);
        races.add(index, raceInLeaderboardDTO);
    }
    
    public RaceColumnDTO createEmptyRaceColumn(String raceColumnName, boolean medalRace) {
        RaceColumnDTO raceColumn = new RaceColumnDTO();
        raceColumn.setRaceColumnName(raceColumnName);
        raceColumn.setMedalRace(medalRace);
        races.add(raceColumn);
        return raceColumn;
    }

    protected RaceColumnDTO getRaceInLeaderboardByName(String raceColumnName) {
        for (RaceColumnDTO race : races) {
            if (race.getRaceColumnName().equals(raceColumnName)) {
                return race;
            }
        }
        return null;
    }

    public List<RaceColumnDTO> getRaceList() {
        return races;
    }

    public boolean raceListContains(String raceColumnName) {
        return getRaceInLeaderboardByName(raceColumnName) != null;
    }

    public void moveRaceUp(String raceColumnName) {
        RaceColumnDTO race = getRaceInLeaderboardByName(raceColumnName);
        int index = races.indexOf(race);
        index--;
        if (index >= 0) {
            races.remove(index + 1);
            races.add(index, race);
        }
    }

    public void moveRaceDown(String raceColumnName) {
        RaceColumnDTO race = getRaceInLeaderboardByName(raceColumnName);
        int index = races.indexOf(race);
        if (index != -1) {
            index++;
            if (index < races.size()) {
                races.remove(index - 1);
                races.add(index, race);
            }
        }
    }

    public void setIsMedalRace(String raceColumnName, boolean medalRace) {
        getRaceInLeaderboardByName(raceColumnName).setMedalRace(medalRace);
    }
    
    /**
     * @return The earliest start date of the races, or <code>null</code> if no start dates of the races are available.
     */
    public Date getStartDate() {
        Date leaderboardStart = null;
        for (RaceColumnDTO race : getRaceList()) {
            for (String fleetName : race.getFleetNames()) {
                Date raceStart = race.getStartDate(fleetName);
                if (raceStart != null) {
                    if (leaderboardStart == null) {
                        leaderboardStart = new Date();
                    } else {
                        leaderboardStart = leaderboardStart.before(raceStart) ? leaderboardStart : raceStart;
                    }
                }
            }
        }
        return leaderboardStart;
    }

    /**
     * Takes the {@link PlacemarkOrderDTO} of all races in this leaderboard, if the PlacemarkOrderDTO for the race is
     * available, and fills all {@link PlacemarkDTO} in a new PlacemarkOrderDTO.<br />
     * The order of the races in this leaderboard determine the order of the PlacemarkDTOs in the PlacemarkOrderDTO.
     * 
     * @return The places of this leaderboard in form of a {@link PlacemarkOrderDTO}, or <code>null</code> if the
     *         {@link PlacemarkOrderDTO places} of no race are available
     */
    public PlacemarkOrderDTO getPlaces() {
        PlacemarkOrderDTO leaderboardPlaces = null;
        for (RaceColumnDTO race : getRaceList()) {
            PlacemarkOrderDTO racePlaces = race.getPlaces();
            if (racePlaces != null) {
                if (leaderboardPlaces == null) {
                    leaderboardPlaces = new PlacemarkOrderDTO();
                }
                leaderboardPlaces.getPlacemarks().addAll(racePlaces.getPlacemarks());
            }
        }
        return leaderboardPlaces;
    }
    
    /**
     * @return <code>true</code> if the leaderboard contains a race which is live
     */
    public boolean containsLiveRace() {
        for (RaceColumnDTO race : getRaceList()) {
            if (race.isLive()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((competitorDisplayNames == null) ? 0 : competitorDisplayNames.hashCode());
        result = prime * result + Arrays.hashCode(discardThresholds);
        result = prime * result + (hasCarriedPoints ? 1231 : 1237);
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((races == null) ? 0 : races.hashCode());
        result = prime * result + ((rows == null) ? 0 : rows.hashCode());
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
        AbstractLeaderboardDTO other = (AbstractLeaderboardDTO) obj;
        if (competitorDisplayNames == null) {
            if (other.competitorDisplayNames != null)
                return false;
        } else if (!competitorDisplayNames.equals(other.competitorDisplayNames))
            return false;
        if (!Arrays.equals(discardThresholds, other.discardThresholds))
            return false;
        if (hasCarriedPoints != other.hasCarriedPoints)
            return false;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        if (races == null) {
            if (other.races != null)
                return false;
        } else if (!races.equals(other.races))
            return false;
        if (rows == null) {
            if (other.rows != null)
                return false;
        } else if (!rows.equals(other.rows))
            return false;
        return true;
    }

    public boolean isDisplayNameSet(CompetitorDTO competitor) {
        return competitorDisplayNames.get(competitor) != null;
    }

}
