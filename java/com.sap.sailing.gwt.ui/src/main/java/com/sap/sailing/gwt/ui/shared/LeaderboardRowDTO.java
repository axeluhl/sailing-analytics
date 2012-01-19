package com.sap.sailing.gwt.ui.shared;

import java.util.Map;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Holds data about one competitor and all races represented by the owning {@link LeaderboardDTO leaderboard}.
 * 
 * @author Axel Uhl (D043530)
 *
 */
public class LeaderboardRowDTO implements IsSerializable {
    public CompetitorDTO competitor;
    public Map<String, LeaderboardEntryDTO> fieldsByRaceName;
    public Integer carriedPoints;
}
