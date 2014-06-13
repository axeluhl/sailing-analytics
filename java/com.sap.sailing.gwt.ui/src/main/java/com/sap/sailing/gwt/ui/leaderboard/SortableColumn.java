package com.sap.sailing.gwt.ui.leaderboard;

import com.google.gwt.cell.client.Cell;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.Header;
import com.sap.sailing.domain.common.InvertibleComparator;
import com.sap.sailing.domain.common.SortingOrder;

public abstract class SortableColumn<T, C> extends Column<T, C> {
    private SortingOrder preferredSortingOrder;
    private final DisplayedLeaderboardRowsProvider displayedLeaderboardRowsProvider;
    
    protected SortableColumn(Cell<C> cell, SortingOrder preferredSortingOrder,
            DisplayedLeaderboardRowsProvider displayedLeaderboardRowsProvider) {
        super(cell);
        setSortable(true);
        this.preferredSortingOrder = preferredSortingOrder;
        this.displayedLeaderboardRowsProvider = displayedLeaderboardRowsProvider;
    }
    
    /**
     * Based on the rows returned by the {@link DisplayedLeaderboardRowsProvider} instance passed to the
     * constructor, updates the min/max values for this column.
     */
    protected void updateMinMax() {}
    
    protected DisplayedLeaderboardRowsProvider getDisplayedLeaderboardRowsProvider() {
        return displayedLeaderboardRowsProvider;
    }
    
    public abstract InvertibleComparator<T> getComparator();
    
    public abstract Header<?> getHeader();

    /**
     * Allows a column to specify a style/CSS class to use to format the &lt;th&gt; header cell.
     * This default implementation returns <code>null</code>, meaning that no additional style other
     * than the GWT-provided default styles will be used.
     */
    public String getHeaderStyle() {
        return null;
    }
    
    /**
     * Allows a column to specify a style/CSS class to use to format the &lt;col&gt; element.
     * This default implementation returns <code>null</code>, meaning that no additional style other
     * than the GWT-provided default styles will be used.
     */
    public String getColumnStyle() {
        return null;
    }

    public SortingOrder getPreferredSortingOrder() {
        return preferredSortingOrder;
    }
}
