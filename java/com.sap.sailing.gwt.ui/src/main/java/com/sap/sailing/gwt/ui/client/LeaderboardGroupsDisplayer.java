package com.sap.sailing.gwt.ui.client;

import java.util.Map;

import com.sap.sailing.gwt.ui.shared.LeaderboardGroupDTO;

public interface LeaderboardGroupsDisplayer {
    void fillLeaderboardGroups(Iterable<LeaderboardGroupDTO> leaderboardGroups);

    void setupLeaderboardGroups(Map<String, String> params);
}
