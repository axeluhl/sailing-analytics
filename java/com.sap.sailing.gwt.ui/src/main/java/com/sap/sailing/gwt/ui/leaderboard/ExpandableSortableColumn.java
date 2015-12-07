package com.sap.sailing.gwt.ui.leaderboard;

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
import com.sap.sailing.domain.common.DetailType;
import com.sap.sailing.domain.common.SortingOrder;
import com.sap.sailing.domain.common.dto.LeaderboardRowDTO;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.client.shared.controls.AbstractSortableColumnWithMinMax;

/**
 * A column that is sortable and offers an expand/collapse button in its column header.
 *  
 * @author Axel Uhl (D043530)
 *
 */
public abstract class ExpandableSortableColumn<C> extends LeaderboardSortableColumnWithMinMax<LeaderboardRowDTO, C> {
    private boolean enableExpansion;
    private boolean togglingInProcess;
    private boolean suppressSortingOnce;
    private final LeaderboardPanel leaderboardPanel;
    private final Map<DetailType, AbstractSortableColumnWithMinMax<LeaderboardRowDTO, ?>> detailColumnsMap;
    private final List<DetailType> detailSelection;
    
    /**
     * Holds the child columns that represent expanded information for this column. If <code>null</code>,
     * the expanded set of columns hasn't been requested yet or cannot be computed because this column
     * is not {@link #isExpansionEnabled() expandable}. Once computed, the child columns remain in this
     * collection and are dynamically inserted to and removed from the {@link CellTable} to the right
     * of this column.
     */
    protected List<AbstractSortableColumnWithMinMax<LeaderboardRowDTO, ?>> directChildren;
    
    /**
     * Tells if this race column is currently displayed in expanded form which includes a visualization
     * of the race's legs.
     */
    private boolean expanded;

    public ExpandableSortableColumn(LeaderboardPanel leaderboardPanel, boolean enableExpansion, Cell<C> cell,
            SortingOrder preferredSortingOrder, StringMessages stringConstants, String detailHeaderStyle, String detailColumnStyle,
            List<DetailType> detailSelection, DisplayedLeaderboardRowsProvider displayedLeaderboardRowsProvider) {
        super(cell, preferredSortingOrder, displayedLeaderboardRowsProvider);
        this.enableExpansion = enableExpansion;
        this.leaderboardPanel = leaderboardPanel;
        this.detailSelection = detailSelection;
        detailColumnsMap = getDetailColumnMap(leaderboardPanel, stringConstants, detailHeaderStyle, detailColumnStyle);
    }
    
    /**
     * By default, an expandable sortable column has no detail columns. Subclasses that want to offer detail columns must
     * override this method.
     */
    protected Map<DetailType, AbstractSortableColumnWithMinMax<LeaderboardRowDTO, ?>> getDetailColumnMap(
            LeaderboardPanel leaderboardPanel, StringMessages stringConstants, String detailHeaderStyle,
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
     * Fetches the cached {@link #directChildren}. Usually, no children are to be returned if the column is not
     * {@link #isExpanded() expanded}. If <code>null</code>, the child columns are determined by calling
     * {@link #createExpansionColumns} and cached in {@link #directChildren}. RaceName of Columns like
     * ManeuverCountRaceColumn is not known at this point because it will be later set in the constructor of
     * TextRaceColumn
     */
    protected Iterable<AbstractSortableColumnWithMinMax<LeaderboardRowDTO, ?>> getDirectChildren() {
        List<AbstractSortableColumnWithMinMax<LeaderboardRowDTO, ?>> result;
        if (isExpanded()) {
            result = new ArrayList<AbstractSortableColumnWithMinMax<LeaderboardRowDTO,?>>();
            for (DetailType detailColumnType : detailSelection) {
                AbstractSortableColumnWithMinMax<LeaderboardRowDTO, ?> selectedColumn = detailColumnsMap.get(detailColumnType);
                if (selectedColumn != null) {
                    result.add(selectedColumn);
                }
            }
        } else {
            result = Collections.emptyList();
        }
        return result;
    }
    
    protected AbstractSortableColumnWithMinMax<LeaderboardRowDTO, ?> createExpansionColumn(DetailType detailColumnType) {
        throw new RuntimeException("Detail column type "+detailColumnType+" not supported by column of type "+getClass().getName());
    }
    
    /**
     * @return the list of details supported by {@link #createExpansionColumn}
     */
    protected List<DetailType> getSupportedDetails() {
        return Collections.emptyList();
    }

    /**
     * Determines the direct and transitive child columns that due to the current expansion state should be
     * visible. Note that for columns not currently visible or currently being expanded (see {@link #toggleExpansion()}),
     * the column collection returned does not necessarily contain only columns really part of the {@link CellTable}
     * used to display this column. 
     */
    private Collection<AbstractSortableColumnWithMinMax<LeaderboardRowDTO, ?>> getAllVisibleChildren() {
        List<AbstractSortableColumnWithMinMax<LeaderboardRowDTO, ?>> transitiveChildren = new ArrayList<>();
        if (isExpanded()) {
            for (AbstractSortableColumnWithMinMax<LeaderboardRowDTO, ?> childColumn : getDirectChildren()) {
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
            final boolean oldBusyState = getLeaderboardPanel().isBusy(); 
            getLeaderboardPanel().setBusyState(true);
            setTogglingInProcess(true);
            final CellTable<LeaderboardRowDTO> table = getLeaderboardPanel().getLeaderboardTable();
            if (isExpanded()) {
                for (AbstractSortableColumnWithMinMax<LeaderboardRowDTO, ?> column : getAllVisibleChildren()) {
                    getLeaderboardPanel().removeColumn(column); // removes only the children currently displayed
                }
                getLeaderboardPanel().setBusyState(false);
                setTogglingInProcess(false);
                // important: toggle expanded state after asking for all visible children
                setExpanded(!isExpanded());
            } else {
                // important: toggle expanded state before asking for all visible children
                setExpanded(!isExpanded());
                ensureExpansionDataIsLoaded(new Runnable() {
                    public void run() {
                        int insertIndex = table.getColumnIndex(ExpandableSortableColumn.this);
                        // The check "insertIndex != -1" is necessary, because the child-columns might be deleted asynchronously
                        // while toggling the columns.
                        if (insertIndex != -1) {
                            insertIndex++;
                            for (AbstractSortableColumnWithMinMax<LeaderboardRowDTO, ?> column : getAllVisibleChildren()) {
                                column.updateMinMax();
                                getLeaderboardPanel().insertColumn(insertIndex++, column);
                            }
                            getLeaderboardPanel().getLeaderboardTable().redraw();
                        }
                        getLeaderboardPanel().setBusyState(oldBusyState);
                        setTogglingInProcess(false);
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
    
    public boolean isTogglingInProcess() {
        return togglingInProcess;
    }
    
    private void setTogglingInProcess(boolean togglingInProcess) {
        this.togglingInProcess = togglingInProcess;
    }

    public void setEnableExpansion(boolean enableExpansion) {
        this.enableExpansion = enableExpansion;
    }

    protected void defaultRender(Context context, LeaderboardRowDTO object, SafeHtmlBuilder html) {
        super.render(context, object, html);
    }
    
    @Override
    public abstract Header<SafeHtml> getHeader();

}
