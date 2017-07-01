package com.sap.sailing.gwt.ui.client.shared.controls;

import com.sap.sailing.gwt.ui.leaderboard.LeaderboardTableResources;
import com.sap.sailing.gwt.ui.leaderboard.SortedCellTableWithStylableHeaders;

/**
 * This {@link FlushableSortedCellTableWithStylableHeaders} provides the
 * {@link FlushableSortedCellTableWithStylableHeaders#flush()}-method for the {@link SelectionCheckboxColumn}. So the
 * {@link SelectionCheckboxColumn} can ensure that the selection state is displayed correct.
 * 
 * @author D064976
 * @param <T>
 */
public class FlushableSortedCellTableWithStylableHeaders<T> extends SortedCellTableWithStylableHeaders<T>
        implements Flushable {
    public FlushableSortedCellTableWithStylableHeaders(int pageSize, LeaderboardTableResources resources) {
        super(pageSize, resources);
    }
}
