package com.sap.sailing.gwt.ui.client;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.google.gwt.cell.client.Cell;
import com.google.gwt.cell.client.Cell.Context;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Header;
import com.sap.sailing.gwt.ui.shared.LeaderboardRowDAO;

/**
 * A column that is sortable and offers an expand/collapse button in its column header.
 *  
 * @author Axel Uhl (D043530)
 *
 */
public abstract class ExpandableSortableColumn<C> extends SortableColumn<LeaderboardRowDAO, C> {
    private boolean enableExpansion;
    private boolean suppressSortingOnce;
    private final LeaderboardPanel leaderboardPanel;
    private final Map<DetailColumnType, SortableColumn<LeaderboardRowDAO, ?>> detailColumnsMap;
    private final List<DetailColumnType> detailSelection;
    
    /**
     * Holds the child columns that represent expanded information for this column. If <code>null</code>,
     * the expanded set of columns hasn't been requested yet or cannot be computed because this column
     * is not {@link #isExpansionEnabled() expandable}. Once computed, the child columns remain in this
     * collection and are dynamically inserted to and removed from the {@link CellTable} to the right
     * of this column.
     */
    protected List<SortableColumn<LeaderboardRowDAO, ?>> directChildren;
    
    /**
     * Tells if this race column is currently displayed in expanded form which includes a visualization
     * of the race's legs.
     */
    private boolean expanded;

    public ExpandableSortableColumn(LeaderboardPanel leaderboardPanel, boolean enableExpansion, Cell<C> cell,
            StringConstants stringConstants, String detailHeaderStyle, String detailColumnStyle,
            List<DetailColumnType> detailSelection) {
        super(cell);
        this.enableExpansion = enableExpansion;
        this.leaderboardPanel = leaderboardPanel;
        this.detailSelection = detailSelection;
        detailColumnsMap = getDetailColumnMap(leaderboardPanel, stringConstants, detailHeaderStyle, detailColumnStyle);
    }
    
    /**
     * By default, an expandable sortable column has no detail columns. Subclasses that want to offer detail columns must
     * override this method.
     */
    protected Map<DetailColumnType, SortableColumn<LeaderboardRowDAO, ?>> getDetailColumnMap(
            LeaderboardPanel leaderboardPanel, StringConstants stringConstants, String detailHeaderStyle,
            String detailColumnStyle) {
        return Collections.emptyMap();
    }

    protected LeaderboardPanel getLeaderboardPanel() {
        return leaderboardPanel;
    }
    
    public boolean isExpanded() {
        return expanded;
    }

    public void setExpanded(boolean expanded) {
        this.expanded = expanded;
    }

    public void suppressSortingOnce() {
        suppressSortingOnce = true;
    }
    
    /**
     * Fetches the cached {@link #directChildren}. If <code>null</code>, the child columns are determined by calling
     * {@link #createExpansionColumns} and cached in {@link #directChildren}.
     */
    protected Iterable<SortableColumn<LeaderboardRowDAO, ?>> getDirectChildren() {
        List<SortableColumn<LeaderboardRowDAO, ?>> result;
        if (isExpanded()) {
            result = new ArrayList<SortableColumn<LeaderboardRowDAO,?>>();
            for (DetailColumnType detailColumnType : detailSelection) {
                SortableColumn<LeaderboardRowDAO, ?> selectedColumn = detailColumnsMap.get(detailColumnType);
                if (selectedColumn != null) {
                    result.add(selectedColumn);
                }
            }
        } else {
            result = Collections.emptyList();
        }
        return result;
    }
    
    /**
     * Subclasses that use this class with expansion enabled need to override this method to define what their expansion
     * columns look like. They only need to create the direct child columns of this column. This default implementation
     * will return an empty but valid list.
     * 
     * @return a valid but possibly empty list
     */
    protected List<SortableColumn<LeaderboardRowDAO, ?>> createExpansionColumns() {
        return Collections.emptyList();
    }

    protected SortableColumn<LeaderboardRowDAO, ?> createExpansionColumn(DetailColumnType detailColumnType) {
        throw new RuntimeException("Detail column type "+detailColumnType+" not supported by column of type "+getClass().getName());
    }
    
    /**
     * @return the list of details supported by {@link #createExpansionColumn}
     */
    protected List<DetailColumnType> getSupportedDetails() {
        return Collections.emptyList();
    }

    private Collection<SortableColumn<LeaderboardRowDAO, ?>> getAllChildren() {
        List<SortableColumn<LeaderboardRowDAO, ?>> transitiveChildren = new ArrayList<SortableColumn<LeaderboardRowDAO,?>>();
        for (SortableColumn<LeaderboardRowDAO, ?> childColumn : getDirectChildren()) {
            transitiveChildren.add(childColumn);
            if (childColumn instanceof ExpandableSortableColumn<?>) {
                @SuppressWarnings("unchecked")
                ExpandableSortableColumn<C> expandableChild = (ExpandableSortableColumn<C>) childColumn;
                transitiveChildren.addAll(expandableChild.getAllChildren());
            }
        }
        return transitiveChildren;
    }
    
    /**
     * Determines the direct and transitive child columns that due to the current expansion state should be
     * visible. Note that for columns not currently visible or currently being expanded (see {@link #toggleExpansion()}),
     * the column collection returned does not necessarily contain only columns really part of the {@link CellTable}
     * used to display this column. 
     */
    private Collection<SortableColumn<LeaderboardRowDAO, ?>> getAllVisibleChildren() {
        List<SortableColumn<LeaderboardRowDAO, ?>> transitiveChildren = new ArrayList<SortableColumn<LeaderboardRowDAO,?>>();
        if (isExpanded()) {
            for (SortableColumn<LeaderboardRowDAO, ?> childColumn : getDirectChildren()) {
                transitiveChildren.add(childColumn);
                if (childColumn instanceof ExpandableSortableColumn<?>) {
                    @SuppressWarnings("unchecked")
                    ExpandableSortableColumn<C> expandableChild = (ExpandableSortableColumn<C>) childColumn;
                    transitiveChildren.addAll(expandableChild.getAllVisibleChildren());
                }
            }
        }
        return transitiveChildren;
    }
    
    /**
     * Asks this column to add / remove the columns used for its expanded view to its right in the leaderboard panel's
     * table. For non-expandable columns, this default implementation simply does nothing. For expandable columns, the
     * {@link #getAllChildred child columns} are obtained and added to the right of this column.
     * <p>
     * 
     * Precondition: this column must currently be contained in the {@link CellTable} showing the
     * {@link #leaderboardPanel leaderboard}.
     */
    public void toggleExpansion() {
        if (isExpansionEnabled()) {
            final CellTable<LeaderboardRowDAO> table = getLeaderboardPanel().getLeaderboardTable();
            if (isExpanded()) {
                for (SortableColumn<LeaderboardRowDAO, ?> column : getAllVisibleChildren()) {
                    int columnIndex = table.getColumnIndex(column);
                    // remove only the children currently displayed
                    if (columnIndex >= 0) {
                        getLeaderboardPanel().removeColumn(columnIndex);
                    }
                }
                // important: toggle expanded state after asking for all visible children
                setExpanded(!isExpanded());
            } else {
                // important: toggle expanded state before asking for all visible children
                setExpanded(!isExpanded());
                ensureExpansionDataIsLoaded(new Runnable() {
                    public void run() {
                        int insertIndex = table.getColumnIndex(ExpandableSortableColumn.this) + 1;
                        for (SortableColumn<LeaderboardRowDAO, ?> column : getAllVisibleChildren()) {
                            column.updateMinMax(getLeaderboardPanel().getLeaderboard());
                            getLeaderboardPanel().insertColumn(insertIndex++, column);
                        }
                        getLeaderboardPanel().getLeaderboardTable().redraw();
                    }
                });
            }
        }
    }
    
    /**
     * Called to ensure that all data necessary to display the expanded data of this column is actually loaded.
     * If this is not yet the case, an asynchronous call to the server may be required that subclasses have to
     * implement. When the data has successfully been loaded, <code>callWhenExpansionDataIsLoaded</code>'s
     * {@link Runnable#run() run} method must be called to create.<p>
     * 
     * This default implementation assumes that all data necessary is already loaded and therefore immediately
     * calls <code>callWhenExpansionDataIsLoaded.run()</code>.
     */
    protected void ensureExpansionDataIsLoaded(Runnable callWhenExpansionDataIsLoaded) {
        callWhenExpansionDataIsLoaded.run();
    }

    @Override
    public boolean isSortable() {
        boolean result;
        if (suppressSortingOnce) {
            result = false;
            suppressSortingOnce = false;
        } else {
            result = super.isSortable();
        }
        return result;
    }

    public boolean isExpansionEnabled() {
        return enableExpansion;
    }

    public void setEnableLegDrillDown(boolean enableLegDrillDown) {
        this.enableExpansion = enableLegDrillDown;
    }

    protected void defaultRender(Context context, LeaderboardRowDAO object, SafeHtmlBuilder html) {
        super.render(context, object, html);
    }
    
    @Override
    public abstract Header<SafeHtml> getHeader();

}
