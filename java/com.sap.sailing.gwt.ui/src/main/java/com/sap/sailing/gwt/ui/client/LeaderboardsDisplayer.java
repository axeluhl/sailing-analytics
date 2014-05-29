package com.sap.sailing.gwt.ui.client;

import com.sap.sailing.gwt.ui.shared.StrippedLeaderboardDTO;

public interface LeaderboardsDisplayer {
    void fillLeaderboards(Iterable<StrippedLeaderboardDTO> leaderboards);
}
