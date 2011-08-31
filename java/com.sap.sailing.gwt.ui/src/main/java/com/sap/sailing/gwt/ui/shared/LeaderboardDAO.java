package com.sap.sailing.gwt.ui.shared;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Captures the serializable properties of a leaderboard which in particular has the competitors, races
 * and their net / total points as well as possible reasons for maximum points (DNS, DNF, DSQ).
 * 
 * @author Axel Uhl (d043530)
 *
 */
public class LeaderboardDAO implements IsSerializable {
    public String name;
    public List<CompetitorDAO> competitors;
    public LinkedHashMap<String, Boolean> raceNamesAndMedalRace;
    public Map<CompetitorDAO, LeaderboardRowDAO> rows;
    public boolean hasCarriedPoints;
    public int[] discardThresholds;
    
    private boolean competitorsOrderedAccordingToTotalRank;
    
    private final TotalRankingComparator totalRankingComparator;
    
    public LeaderboardDAO() {
        totalRankingComparator = new TotalRankingComparator();
        competitorsOrderedAccordingToTotalRank = false;
    }
    
    public TotalRankingComparator getTotalRankingComparator() {
        return totalRankingComparator;
    }

    public boolean scoredInMedalRace(CompetitorDAO competitor) {
        LeaderboardRowDAO row = rows.get(competitor);
        for (Map.Entry<String, Boolean> raceNameAndMedalRace : raceNamesAndMedalRace.entrySet()) {
            if (raceNameAndMedalRace.getValue() && row.fieldsByRaceName.get(raceNameAndMedalRace.getKey()).totalPoints > 0) {
                return true;
            }
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
                }
                
            }
            return result;
        }
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
        return competitors.indexOf(competitor);
    };

}
