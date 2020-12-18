package com.sap.sailing.gwt.ui.client;

import com.sap.sailing.gwt.ui.shared.StrippedLeaderboardDTO;

public interface LeaderboardsDisplayer<T extends StrippedLeaderboardDTO> extends Displayer {
    void fillLeaderboards(Iterable<T> leaderboards);
}
