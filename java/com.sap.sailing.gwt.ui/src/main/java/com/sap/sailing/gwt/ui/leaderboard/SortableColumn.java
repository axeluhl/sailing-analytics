package com.sap.sailing.gwt.ui.leaderboard;

import java.util.Comparator;

import com.google.gwt.cell.client.Cell;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortList;
import com.google.gwt.user.cellview.client.Header;
import com.sap.sailing.domain.common.SortingOrder;
import com.sap.sailing.gwt.ui.shared.LeaderboardDTO;
import com.sap.sailing.gwt.ui.shared.LeaderboardRowDTO;

public abstract class SortableColumn<T, C> extends Column<T, C> {
    private SortingOrder preferredSortingOrder;
    
    protected SortableColumn(Cell<C> cell) {
        super(cell);
        setSortable(true);
        this.preferredSortingOrder = SortingOrder.NONE;
    }

    protected SortableColumn(Cell<C> cell, SortingOrder preferredSortingOrder) {
        super(cell);
        setSortable(true);
        this.preferredSortingOrder = preferredSortingOrder;
    }
    
    protected void updateMinMax(LeaderboardDTO leaderboard) {}
    
    /**
     * To enable sorting of <code>null</code> values to the end even in ascending order, the comparators provided
     * by the subclasses will want to know whether the leaderboard table is currently sorted in ascending order
     * for this column.
     */
    protected boolean isSortedAscendingForThisColumn(CellTable<LeaderboardRowDTO> leaderboardTable) {
        boolean result = true;
        ColumnSortList sortList = leaderboardTable.getColumnSortList();
        SortingOrder sortingOrder = preferredSortingOrder;
        if(sortList.size() > 0 && sortList.get(0).getColumn() == this) {
            sortingOrder = sortList.get(0).isAscending() ? SortingOrder.ASCENDING : SortingOrder.DESCENDING;
        }
        
        if(sortingOrder == SortingOrder.DESCENDING)
            result = false;
        
        return result;
    }
    
    public abstract Comparator<T> getComparator();
    
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
