package com.sap.sailing.gwt.ui.shared;

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
    public List<String> races;
    public Map<Pair<CompetitorDAO, String>, LeaderboardEntryDAO> fields;
    public Map<CompetitorDAO, Integer> carryPoints;
}
