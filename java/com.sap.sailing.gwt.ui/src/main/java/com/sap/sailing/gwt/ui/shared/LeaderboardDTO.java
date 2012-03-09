package com.sap.sailing.gwt.ui.shared;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.google.gwt.user.client.rpc.IsSerializable;
import com.sap.sailing.domain.common.RaceIdentifier;

/**
 * Captures the serializable properties of a leaderboard which in particular has the competitors, any optional display
 * name mappings for the competitors, races and their net / total points as well as possible reasons for maximum points
 * (DNS, DNF, DSQ).
 * 
 * @author Axel Uhl (d043530)
 *  
 */
public class LeaderboardDTO implements IsSerializable {
    public String name;
    public List<CompetitorDTO> competitors;
    private List<RaceInLeaderboardDTO> races;
    public Map<CompetitorDTO, String> competitorDisplayNames;
    public Map<CompetitorDTO, LeaderboardRowDTO> rows;
    public boolean hasCarriedPoints;
    public int[] discardThresholds;

    private boolean competitorsOrderedAccordingToTotalRank;

    private final transient TotalRankingComparator totalRankingComparator;

    public LeaderboardDTO() {
        totalRankingComparator = new TotalRankingComparator();
        competitorsOrderedAccordingToTotalRank = false;
        races = new ArrayList<RaceInLeaderboardDTO>();
    }

    public String getDisplayName(CompetitorDTO competitor) {
        if (competitorDisplayNames == null || competitorDisplayNames.get(competitor) == null) {
            return competitor.name;
        } else {
            return competitorDisplayNames.get(competitor);
        }
    }

    public Comparator<LeaderboardRowDTO> getTotalRankingComparator() {
        return totalRankingComparator;
    }

    public Comparator<LeaderboardRowDTO> getMedalRaceComparator(String medalRaceName) {
        return new MedalRaceComparator(medalRaceName);
    }

    /**
     * If the race whose name is specified in <code>raceName</code> has any competitor who has valid
     * {@link LeaderboardEntryDTO#legDetails} for that race, the number of entries in the leg details is returned,
     * telling the number of legs that the race has. Otherwise, -1 is returned.
     */
    public int getLegCount(String raceName) {
        for (LeaderboardRowDTO row : rows.values()) {
            if (row.fieldsByRaceName.get(raceName) != null && row.fieldsByRaceName.get(raceName).legDetails != null) {
                return row.fieldsByRaceName.get(raceName).legDetails.size();
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
        for (RaceInLeaderboardDTO race : races) {
            if (race.isMedalRace() && row.fieldsByRaceName.get(race.getRaceColumnName()).totalPoints > 0) {
                return true;
            }
        }
        return false;
    }

    public int getTotalPoints(LeaderboardRowDTO object) {
        int totalPoints = object.carriedPoints == null ? 0 : object.carriedPoints;
        for (LeaderboardEntryDTO e : object.fieldsByRaceName.values()) {
            totalPoints += e.totalPoints;
        }
        return totalPoints;
    }

    private class TotalRankingComparator implements Comparator<LeaderboardRowDTO> {
        @Override
        public int compare(LeaderboardRowDTO o1, LeaderboardRowDTO o2) {
            int result;
            if (o1 == null && o2 == null) {
                result = 0;
            } else if (o1 == null) {
                result = -1;
            } else if (o2 == null) {
                result = 1;
            } else {
                if (scoredInMedalRace(o1.competitor)) {
                    if (scoredInMedalRace(o2.competitor)) {
                        // both scored in medal race
                        result = getTotalPoints(o1) - getTotalPoints(o2);
                        // in case of tie, medal race points decide:
                        if (result == 0) {
                            result = getMedalRaceScore(o1.competitor) - getMedalRaceScore(o2.competitor);
                        }
                    } else {
                        // only o1 scored in medal race, so o1 scores better = "less"
                        result = -1;
                    }
                } else {
                    if (scoredInMedalRace(o2.competitor)) {
                        // only o2 scored in medal race, so o2 scores better, o1 scores worse = "greater"
                        result = 1;
                    } else {
                        // neither one scored in any medal race
                        result = getTotalPoints(o1) - getTotalPoints(o2);
                        // Now if both have equal points, count races won.
                        if (result == 0) {
                            result = getNumberOfRacesWon(o2.competitor) - getNumberOfRacesWon(o1.competitor);
                        }
                        // If number of races won is still equal, use rank in last race where at least one of the two
                        // competitors was assigned a score
                        if (result == 0) {
                            String nameOfLastRaceSoFar = getNameOfLastRaceSoFar(o1.competitor, o2.competitor);
                            int netPoints1 = getNetPoints(o1.competitor, nameOfLastRaceSoFar);
                            int netPoints2 = getNetPoints(o2.competitor, nameOfLastRaceSoFar);
                            result = netPoints1 == 0 ? netPoints2 == 0 ? 0 : -1 : netPoints2 == 0 ? 1 : netPoints1
                                    - netPoints2;
                        }
                    }
                }
            }
            return result;
        }
    }

    private class MedalRaceComparator implements Comparator<LeaderboardRowDTO> {
        private final String medalRaceName;

        public MedalRaceComparator(String medalRaceName) {
            this.medalRaceName = medalRaceName;
        }

        @Override
        public int compare(LeaderboardRowDTO o1, LeaderboardRowDTO o2) {
            int result;
            if (scoredInMedalRace(o1.competitor)) {
                if (scoredInMedalRace(o2.competitor)) {
                    // both scored in medal race
                    result = o1.fieldsByRaceName.get(medalRaceName).netPoints
                            - o2.fieldsByRaceName.get(medalRaceName).netPoints;
                } else {
                    // only o1 scored in medal race, so o1 scores better = "less"
                    result = -1;
                }
            } else {
                if (scoredInMedalRace(o2.competitor)) {
                    // only o2 scored in medal race, so o2 scores better, o1 scores worse = "greater"
                    result = 1;
                } else {
                    // neither one scored in any medal race; to be considered equal for medal race comparison
                    result = 0;
                }

            }
            return result;
        }
    }

    /**
     * To be called after something was incrementally altered in this leaderboard that may affect the competitor
     * ranking, in particular anything score related. Probably the only change that wouldn't affect the ordering is a
     * name change.
     */
    public void invalidateCompetitorOrdering() {
        competitorsOrderedAccordingToTotalRank = false;
    }

    /**
     * Sums up the net points <code>competitor</code> scored in any medal races
     */
    private int getMedalRaceScore(CompetitorDTO competitor) {
        int result = 0;
        LeaderboardRowDTO row = rows.get(competitor);
        for (RaceInLeaderboardDTO race : races) {
            if (race.isMedalRace() && row.fieldsByRaceName.containsKey(race.getRaceColumnName())) {
                result += row.fieldsByRaceName.get(race.getRaceColumnName()).netPoints;
            }
        }
        return result;
    }

    public int getNetPoints(CompetitorDTO competitor, String nameOfLastRaceSoFar) {
        int result = 0;
        LeaderboardRowDTO row = rows.get(competitor);
        if (row != null) {
            LeaderboardEntryDTO field = row.fieldsByRaceName.get(nameOfLastRaceSoFar);
            if (field != null) {
                result = field.netPoints;
            }
        }
        return result;
    }

    /**
     * Find the name of the last race in {@link #raceNamesAndMedalRaceAndTracked}'s keys for which both, <code>c1</code>
     * and <code>c2</code> have been assigned a score.
     */
    private String getNameOfLastRaceSoFar(CompetitorDTO c1, CompetitorDTO c2) {
        String nameOfLastRaceSoFar = null;
        for (RaceInLeaderboardDTO race : races) {
            for (LeaderboardRowDTO row : rows.values()) {
                if (row.competitor.equals(c1) || row.competitor.equals(c2)) {
                    LeaderboardEntryDTO leaderboardEntryDTO = row.fieldsByRaceName.get(race.getRaceColumnName());
                    if (leaderboardEntryDTO != null && leaderboardEntryDTO.netPoints != 0) {
                        nameOfLastRaceSoFar = race.getRaceColumnName();
                        break;
                    }
                }
            }
        }
        return nameOfLastRaceSoFar;
    }

    private int getNumberOfRacesWon(CompetitorDTO competitor) {
        int result = 0;
        LeaderboardRowDTO row = rows.get(competitor);
        if (row != null) {
            for (RaceInLeaderboardDTO race : races) {
                LeaderboardEntryDTO field = row.fieldsByRaceName.get(race.getRaceColumnName());
                if (field != null && field.netPoints == 1) {
                    result++;
                }
            }
        }
        return result;
    }

    public int getRank(CompetitorDTO competitor) {
        if (!competitorsOrderedAccordingToTotalRank) {
            Collections.sort(competitors, new Comparator<CompetitorDTO>() {
                @Override
                public int compare(CompetitorDTO o1, CompetitorDTO o2) {
                    return getTotalRankingComparator().compare(rows.get(o1), rows.get(o2));
                }
            });
            competitorsOrderedAccordingToTotalRank = true;
        }
        return competitors.indexOf(competitor) + 1;
    };

    public boolean raceIsTracked(String raceColumnName) {
        for (RaceInLeaderboardDTO race : races) {
            if (race.getRaceColumnName().equals(raceColumnName)) {
                return race.isTrackedRace();
            }
        }
        return false;

    }

    public boolean raceIsMedalRace(String raceColumnName) {
        return getRaceInLeaderboardByName(raceColumnName).isMedalRace();
    }

    public void addRace(String raceColumnName, boolean medalRace, RaceIdentifier trackedRaceIdentifier, StrippedRaceDTO race) {
        RaceInLeaderboardDTO raceInLeaderboardDTO = new RaceInLeaderboardDTO();
        raceInLeaderboardDTO.setRaceColumnName(raceColumnName);
        raceInLeaderboardDTO.setMedalRace(medalRace);
        raceInLeaderboardDTO.setRaceIdentifier(trackedRaceIdentifier);
        raceInLeaderboardDTO.setRace(race);
    	races.add(raceInLeaderboardDTO);
    }

    public void addRaceAt(String raceColumnName, boolean medalRace, RaceIdentifier trackedRaceIdentifier, int index) {
        RaceInLeaderboardDTO raceInLeaderboardDTO = new RaceInLeaderboardDTO();
        raceInLeaderboardDTO.setRaceColumnName(raceColumnName);
        raceInLeaderboardDTO.setMedalRace(medalRace);
        raceInLeaderboardDTO.setRaceIdentifier(trackedRaceIdentifier);
        races.add(index, raceInLeaderboardDTO);
    }

    public void removeRace(String raceColumnName) {
        races.remove(getRaceInLeaderboardByName(raceColumnName));
    }

    public void renameRace(String oldName, String newName) {
        RaceInLeaderboardDTO race = getRaceInLeaderboardByName(oldName);
        race.setRaceColumnName(newName);
    }

    private RaceInLeaderboardDTO getRaceInLeaderboardByName(String raceColumnName) {
        for (RaceInLeaderboardDTO race : races) {
            if (race.getRaceColumnName().equals(raceColumnName)) {
                return race;
            }
        }
        return null;
    }

    public List<RaceInLeaderboardDTO> getRaceList() {
        return races;
    }

    public boolean raceListContains(String raceColumnName) {
        return getRaceInLeaderboardByName(raceColumnName) != null;
    }

    public void moveRaceUp(String raceColumnName) {
        RaceInLeaderboardDTO race = getRaceInLeaderboardByName(raceColumnName);
        int index = races.indexOf(race);
        index--;
        if (index >= 0) {
            races.remove(index + 1);
            races.add(index, race);
        }
    }

    public void moveRaceDown(String raceColumnName) {
        RaceInLeaderboardDTO race = getRaceInLeaderboardByName(raceColumnName);
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
        for (RaceInLeaderboardDTO race : getRaceList()) {
            Date raceStart = race.getStartDate();
            if (raceStart != null) {
                if (leaderboardStart == null) {
                    leaderboardStart = new Date();
                }
                leaderboardStart = leaderboardStart.before(raceStart) ? leaderboardStart : raceStart;
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
        for (RaceInLeaderboardDTO race : getRaceList()) {
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

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((competitorDisplayNames == null) ? 0 : competitorDisplayNames.hashCode());
        result = prime * result + ((competitors == null) ? 0 : competitors.hashCode());
        result = prime * result + (competitorsOrderedAccordingToTotalRank ? 1231 : 1237);
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
        LeaderboardDTO other = (LeaderboardDTO) obj;
        if (competitorDisplayNames == null) {
            if (other.competitorDisplayNames != null)
                return false;
        } else if (!competitorDisplayNames.equals(other.competitorDisplayNames))
            return false;
        if (competitors == null) {
            if (other.competitors != null)
                return false;
        } else if (!competitors.equals(other.competitors))
            return false;
        if (competitorsOrderedAccordingToTotalRank != other.competitorsOrderedAccordingToTotalRank)
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

}
