package com.sap.sailing.gwt.ui.client;

import com.sap.sailing.gwt.ui.shared.LeaderboardGroupDTO;

public interface LeaderboardGroupsDisplayer {
    void fillLeaderboardGroups(Iterable<LeaderboardGroupDTO> leaderboardGroups);
}
