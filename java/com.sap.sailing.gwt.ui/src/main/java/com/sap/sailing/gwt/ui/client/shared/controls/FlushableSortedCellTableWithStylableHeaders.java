package com.sap.sailing.gwt.ui.client.shared.controls;

import com.sap.sailing.gwt.ui.leaderboard.LeaderboardTableResources;
import com.sap.sailing.gwt.ui.leaderboard.SortedCellTableWithStylableHeaders;

public class FlushableSortedCellTableWithStylableHeaders<T> extends SortedCellTableWithStylableHeaders<T>
        implements Flushable {
    public FlushableSortedCellTableWithStylableHeaders(int pageSize, LeaderboardTableResources resources) {
        super(pageSize, resources);
    }
}
