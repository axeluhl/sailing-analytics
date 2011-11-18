package com.sap.sailing.gwt.ui.shared;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Captures the serializable properties of a leaderboard which in particular has the competitors, any optional display
 * name mappings for the competitors, races and their net / total points as well as possible reasons for maximum points
 * (DNS, DNF, DSQ).
 * 
 * @author Axel Uhl (d043530)
 * 
 */
public class LeaderboardDAO implements IsSerializable {
    public String name;
    public List<CompetitorDAO> competitors;
    //public LinkedHashMap<String, Pair<Boolean, Boolean>> raceNamesAndMedalRaceAndTracked;
    private List<String> raceNames;
    private Map<String, Boolean> racesMedalRace;
    private Map<String, Boolean> racesTracked;
    public Map<CompetitorDAO, String> displayNames;
    public Map<CompetitorDAO, LeaderboardRowDAO> rows;
    public boolean hasCarriedPoints;
    public int[] discardThresholds;
    
    private boolean competitorsOrderedAccordingToTotalRank;
    
    private final transient TotalRankingComparator totalRankingComparator;
    
    public LeaderboardDAO() {
        totalRankingComparator = new TotalRankingComparator();
        competitorsOrderedAccordingToTotalRank = false;
        raceNames = new ArrayList<String>();
        racesMedalRace = new HashMap<String, Boolean>();
        racesTracked = new HashMap<String, Boolean>();
    }
    
    public String getDisplayName(CompetitorDAO competitor) {
        if (displayNames == null || displayNames.get(competitor) == null) {
            return competitor.name;
        } else {
            return displayNames.get(competitor);
        }
    }
    
    public Comparator<LeaderboardRowDAO> getTotalRankingComparator() {
        return totalRankingComparator;
    }
    
    public Comparator<LeaderboardRowDAO> getMedalRaceComparator(String medalRaceName) {
        return new MedalRaceComparator(medalRaceName);
    }
    
    /**
     * If the race whose name is specified in <code>raceName</code> has any competitor who has valid {@link LeaderboardEntryDAO#legDetails}
     * for that race, the number of entries in the leg details is returned, telling the number of legs that the race has. Otherwise,
     * -1 is returned.
     */
    public int getLegCount(String raceName) {
        for (LeaderboardRowDAO row : rows.values()) {
            if (row.fieldsByRaceName.get(raceName) != null && row.fieldsByRaceName.get(raceName).legDetails != null) {
                return row.fieldsByRaceName.get(raceName).legDetails.size();
            }
        }
        return -1;
    }

    /**
     * Tells if the <code>competitor</code> scored (and therefore presumably participated) in a medal race
     * represented in this leaderboard.
     */
    public boolean scoredInMedalRace(CompetitorDAO competitor) {
        LeaderboardRowDAO row = rows.get(competitor);
        /*
        for (Map.Entry<String, Pair<Boolean, Boolean>> raceNameAndMedalRace : raceNamesAndMedalRaceAndTracked.entrySet()) {
            if (raceNameAndMedalRace.getValue().getA() && row.fieldsByRaceName.get(raceNameAndMedalRace.getKey()).totalPoints > 0) {
                return true;
            }
        }
        */
        for (String race : raceNames){
        	if (raceIsMedalRace(race) && row.fieldsByRaceName.get(race).totalPoints > 0)
        		return true;
        }
        return false;
    }
    
    public int getTotalPoints(LeaderboardRowDAO object) {
        int totalPoints = object.carriedPoints==null?0:object.carriedPoints;
        for (LeaderboardEntryDAO e : object.fieldsByRaceName.values()) {
            totalPoints += e.totalPoints;
        }
        return totalPoints;
    }

    private class TotalRankingComparator implements Comparator<LeaderboardRowDAO> {
        @Override
        public int compare(LeaderboardRowDAO o1, LeaderboardRowDAO o2) {
            int result;
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
                    // If number of races won is still equal, use rank in last race where at least one of the two competitors was assigned a score
                    if (result == 0) {
                        String nameOfLastRaceSoFar = getNameOfLastRaceSoFar(o1.competitor, o2.competitor);
                        int netPoints1 = getNetPoints(o1.competitor, nameOfLastRaceSoFar);
                        int netPoints2 = getNetPoints(o2.competitor, nameOfLastRaceSoFar);
                        result = netPoints1==0 ? netPoints2==0 ? 0 : -1 : netPoints2==0 ? 1 : netPoints1-netPoints2;
                    }
                }
            }
            return result;
        }
    }
    
    private class MedalRaceComparator implements Comparator<LeaderboardRowDAO> {
        private final String medalRaceName;
        
        public MedalRaceComparator(String medalRaceName) {
            this.medalRaceName = medalRaceName;
        }

        @Override
        public int compare(LeaderboardRowDAO o1, LeaderboardRowDAO o2) {
            int result;
            if (scoredInMedalRace(o1.competitor)) {
                if (scoredInMedalRace(o2.competitor)) {
                    // both scored in medal race
                    result = o1.fieldsByRaceName.get(medalRaceName).netPoints - o2.fieldsByRaceName.get(medalRaceName).netPoints;
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
     * To be called after something was incrementally altered in this leaderboard that may affect the
     * competitor ranking, in particular anything score related. Probably the only change that wouldn't
     * affect the ordering is a name change.
     */
    public void invalidateCompetitorOrdering() {
        competitorsOrderedAccordingToTotalRank = false;
    }

    /**
     * Sums up the net points <code>competitor</code> scored in any medal races
     */
    private int getMedalRaceScore(CompetitorDAO competitor) {
        int result = 0;
        LeaderboardRowDAO row = rows.get(competitor);
        /*
        for (Map.Entry<String, Pair<Boolean, Boolean>> raceNameAndMedalRace : raceNamesAndMedalRaceAndTracked.entrySet()) {
            if (raceNameAndMedalRace.getValue().getA() && row.fieldsByRaceName.containsKey(raceNameAndMedalRace.getKey())) {
                result += row.fieldsByRaceName.get(raceNameAndMedalRace.getKey()).netPoints;
            }
        }
        */
        for (String race : raceNames){
        	if (raceIsMedalRace(race) && row.fieldsByRaceName.containsKey(race))
        		result += row.fieldsByRaceName.get(race).netPoints;
        }
        return result;
    }

    public int getNetPoints(CompetitorDAO competitor, String nameOfLastRaceSoFar) {
        int result = 0;
        LeaderboardRowDAO row = rows.get(competitor);
        if (row != null) {
            LeaderboardEntryDAO field = row.fieldsByRaceName.get(nameOfLastRaceSoFar);
            if (field != null) {
                result = field.netPoints;
            }
        }
        return result;
    }

    /**
     * Find the name of the last race in {@link #raceNamesAndMedalRaceAndTracked}'s keys for which both, <code>c1</code> and
     * <code>c2</code> have been assigned a score.
     */
    private String getNameOfLastRaceSoFar(CompetitorDAO c1, CompetitorDAO c2) {
        String nameOfLastRaceSoFar = null;
        /*
        for (String raceName : raceNamesAndMedalRaceAndTracked.keySet()) {
            for (LeaderboardRowDAO row : rows.values()) {
                if (row.competitor.equals(c1) || row.competitor.equals(c2)) {
                    LeaderboardEntryDAO leaderboardEntryDAO = row.fieldsByRaceName.get(raceName);
                    if (leaderboardEntryDAO != null && leaderboardEntryDAO.netPoints != 0) {
                        nameOfLastRaceSoFar = raceName;
                        break;
                    }
                }
            }
        }
        */
        for (String race : raceNames){
        	for (LeaderboardRowDAO row : rows.values()) {
                if (row.competitor.equals(c1) || row.competitor.equals(c2)) {
                    LeaderboardEntryDAO leaderboardEntryDAO = row.fieldsByRaceName.get(race);
                    if (leaderboardEntryDAO != null && leaderboardEntryDAO.netPoints != 0) {
                        nameOfLastRaceSoFar = race;
                        break;
                    }
                }
            }
        }
        return nameOfLastRaceSoFar;
    }

    private int getNumberOfRacesWon(CompetitorDAO competitor) {
        int result = 0;
        LeaderboardRowDAO row = rows.get(competitor);
        if (row != null) {
        	/*
            for (String raceName : raceNamesAndMedalRaceAndTracked.keySet()) {
                LeaderboardEntryDAO field = row.fieldsByRaceName.get(raceName);
                if (field != null && field.netPoints == 1) {
                    result++;
                }
            }
            */
        	for (String race : raceNames){
        		LeaderboardEntryDAO field = row.fieldsByRaceName.get(race);
                if (field != null && field.netPoints == 1) {
                    result++;
                }
        	}
        }
        return result;
    }

    public int getRank(CompetitorDAO competitor) {
        if (!competitorsOrderedAccordingToTotalRank) {
            Collections.sort(competitors, new Comparator<CompetitorDAO>() {
                @Override
                public int compare(CompetitorDAO o1, CompetitorDAO o2) {
                    return getTotalRankingComparator().compare(rows.get(o1), rows.get(o2));
                }
            });
            competitorsOrderedAccordingToTotalRank = true;
        }
        return competitors.indexOf(competitor)+1;
    };
    
    public boolean raceIsTracked(String raceName){
    	return racesTracked.get(raceName);
    	
    }
    
    public boolean raceIsMedalRace(String raceName){
    	return racesMedalRace.get(raceName);
    }
    
    public void addRace(String name, boolean isMedalRace, boolean isTracked){
    	raceNames.add(name);
    	racesMedalRace.put(name, isMedalRace);
    	racesTracked.put(name, isTracked);
    }
    
    public void addRaceAt(String name, boolean isMedalRace, boolean isTracked, int index){
    	raceNames.add(index, name);
    	racesMedalRace.put(name, isMedalRace);
    	racesTracked.put(name, isTracked);
    }
    
    public void removeRace(String name){
    	racesMedalRace.remove(name);
    	racesTracked.remove(name);
    	raceNames.remove(getRaceIdByName(name));
    }
    
    public void renameRace(String oldName, String newName){
    	int index = getRaceIdByName(oldName);
    	racesMedalRace.put(newName, racesMedalRace.get(oldName));
    	racesTracked.put(newName, racesTracked.get(oldName));
    	racesMedalRace.remove(oldName);
    	racesTracked.remove(oldName);
    	raceNames.set(index, newName);
    }
    
    public List<String> getRaceList(){
    	return raceNames;
    }
    
    public boolean raceListContains(String raceName){
    	if (getRaceIdByName(raceName) == -1)
    		return false;
    	return true;
    }
    
    public int getRaceIdByName(String raceName){
    	for (int i = 0; i < raceNames.size(); i++) {
			if(raceNames.get(i).equals(raceName))
				return i;
		}
    	return -1;
    }
    
    public void moveRaceUp(String raceName){
    	int index = getRaceIdByName(raceName);
    	index--;
    	if (index >= 0){
    		raceNames.remove(index+1);
    		raceNames.add(index, raceName);
    	}
    }
    
    public void moveRaceDown(String raceName){
    	int index = getRaceIdByName(raceName);
    	if (index == -1)
    		return;
    	index++;
    	if (index < raceNames.size()){
    		raceNames.remove(index-1);
    		raceNames.add(index, raceName);
    	}
    }
    
    public void setIsMedalRace(String raceName, boolean isMedalRace){
    	racesMedalRace.put(raceName, isMedalRace);
    }

}
