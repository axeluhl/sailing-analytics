package com.sap.sailing.gwt.ui.shared;

import java.util.HashMap;
import java.util.Map;

import com.google.gwt.user.client.rpc.IsSerializable;
import com.sap.sailing.domain.common.MaxPointsReason;

/**
 * Captures a collection of score corrections to apply to a leaderboard. The leaderboard is identified by name,
 * and so are the race columns. The competitors are identified by their {@link CompetitorDTO#id ID}.
 * 
 * @author Axel Uhl (D043530)
 *
 */
public class BulkScoreCorrectionDTO implements IsSerializable {
    private String leaderboardName;
    private Map<String, Map<String, MaxPointsReason>> maxPointsUpdatesForRaceColumnByCompetitorIdAsString;
    private Map<String, Map<String, Double>> scoreUpdatesForRaceColumnByCompetitorIdAsString;

    public BulkScoreCorrectionDTO() {}
    
    public BulkScoreCorrectionDTO(String leaderboardName) {
        this.leaderboardName = leaderboardName;
        maxPointsUpdatesForRaceColumnByCompetitorIdAsString = new HashMap<String, Map<String,MaxPointsReason>>();
        scoreUpdatesForRaceColumnByCompetitorIdAsString = new HashMap<String, Map<String, Double>>();
    }
    
    public void addScoreUpdate(CompetitorDTO competitor, RaceColumnDTO raceColumn, double newScore) {
        Map<String, Double> map = scoreUpdatesForRaceColumnByCompetitorIdAsString.get(competitor.id);
        if (map == null) {
            map = new HashMap<String, Double>();
            scoreUpdatesForRaceColumnByCompetitorIdAsString.put(competitor.id, map);
        }
        map.put(raceColumn.name, newScore);
    }

    public void addMaxPointsReasonUpdate(CompetitorDTO competitor, RaceColumnDTO raceColumn, MaxPointsReason newReason) {
        Map<String, MaxPointsReason> map = maxPointsUpdatesForRaceColumnByCompetitorIdAsString.get(competitor.id);
        if (map == null) {
            map = new HashMap<String, MaxPointsReason>();
            maxPointsUpdatesForRaceColumnByCompetitorIdAsString.put(competitor.id, map);
        }
        map.put(raceColumn.name, newReason);
    }
    
    public Map<String, Map<String, MaxPointsReason>> getMaxPointsUpdatesForRaceColumnByCompetitorIdAsString() {
        return maxPointsUpdatesForRaceColumnByCompetitorIdAsString;
    }

    public Map<String, Map<String, Double>> getScoreUpdatesForRaceColumnByCompetitorIdAsString() {
        return scoreUpdatesForRaceColumnByCompetitorIdAsString;
    }

    public String getLeaderboardName() {
        return leaderboardName;
    }
}