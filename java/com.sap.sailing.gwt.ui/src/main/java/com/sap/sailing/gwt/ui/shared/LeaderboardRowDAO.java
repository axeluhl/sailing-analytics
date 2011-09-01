package com.sap.sailing.gwt.ui.shared;

import java.util.Map;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Holds data about one competitor and all races represented by the owning {@link LeaderboardDAO leaderboard}.
 * 
 * @author Axel Uhl (D043530)
 *
 */
public class LeaderboardRowDAO implements IsSerializable {
    public CompetitorDAO competitor;
    public Map<String, LeaderboardEntryDAO> fieldsByRaceName;
    public Integer carriedPoints;
}
