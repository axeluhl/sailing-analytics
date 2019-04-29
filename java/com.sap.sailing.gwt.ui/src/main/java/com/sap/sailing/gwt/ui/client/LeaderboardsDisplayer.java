package com.sap.sailing.gwt.ui.client;

import com.sap.sailing.gwt.ui.shared.StrippedLeaderboardDTO;

public interface LeaderboardsDisplayer<T extends StrippedLeaderboardDTO> {
    void fillLeaderboards(Iterable<T> leaderboards);
}
