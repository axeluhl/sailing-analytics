package com.sap.sailing.gwt.ui.shared;

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
    
    public boolean scoredInMedalRace(CompetitorDAO competitor) {
        LeaderboardRowDAO row = rows.get(competitor);
        for (Map.Entry<String, Boolean> raceNameAndMedalRace : raceNamesAndMedalRace.entrySet()) {
            if (raceNameAndMedalRace.getValue() && row.fieldsByRaceName.get(raceNameAndMedalRace.getKey()).totalPoints > 0) {
                return true;
            }
        }
        return false;
    }
}
