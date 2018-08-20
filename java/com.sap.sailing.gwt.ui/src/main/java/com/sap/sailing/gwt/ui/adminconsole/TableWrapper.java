package com.sap.sailing.gwt.ui.adminconsole;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.ColumnSortEvent.ListHandler;
import com.google.gwt.user.cellview.client.SimplePager;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.HasRows;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.Range;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.client.shared.controls.FlushableCellTable;
import com.sap.sailing.gwt.ui.client.shared.controls.SelectionCheckboxColumn;
import com.sap.sse.common.Util;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.celltable.EntityIdentityComparator;
import com.sap.sse.gwt.client.celltable.RefreshableSelectionModel;
import com.sap.sse.gwt.client.celltable.RefreshableSingleSelectionModel;
import com.sap.sse.gwt.client.panels.AbstractFilterablePanel;

/**
 * The {@link #getTable() table} created and wrapped by this object offers already a {@link ListHandler} for sorting.
 * Subclasses can obtain the table's default column sort handler created by this class's constructor by calling
 * {@link #getColumnSortHandler}. The table is wrapped by a panel that can be obtained using {@link #asWidget()}
 * and which contains, if requested, the pager widget underneath the table.
 */
public abstract class TableWrapper<T, S extends RefreshableSelectionModel<T>> implements IsWidget {
    /**
     * If the {@code enablePager} constructur argument is set to {@code true} then this many entries are shown
     * at most on one page, and users will have to flip through the pages one by one.
     */
    protected static final int DEFAULT_PAGING_SIZE = 100;
    
    protected final FlushableCellTable<T> table;
    private S selectionModel;
    protected ListDataProvider<T> dataProvider;
    protected VerticalPanel mainPanel;
    protected final SailingServiceAsync sailingService;
    protected final ErrorReporter errorReporter;
    protected final StringMessages stringMessages;
    private final boolean multiSelection;
    private SelectionCheckboxColumn<T> selectionCheckboxColumn;
    private final EntityIdentityComparator<T> entityIdentityComparator;

    protected final AdminConsoleTableResources tableRes = GWT.create(AdminConsoleTableResources.class);
    private final ListHandler<T> columnSortHandler;
    
    @Override
    public Widget asWidget() {
        return mainPanel;
    }

    /**
     * @param entityIdentityComparator
     *            {@link EntityIdentityComparator} to create a {@link RefreshableSelectionModel}
     */
    public TableWrapper(SailingServiceAsync sailingService, final StringMessages stringMessages,
            ErrorReporter errorReporter, boolean multiSelection, boolean enablePager,
            EntityIdentityComparator<T> entityIdentityComparator) {
        this(sailingService, stringMessages, errorReporter, multiSelection, enablePager, DEFAULT_PAGING_SIZE,
                entityIdentityComparator);
    }

    /**
     * @param entityIdentityComparator
     *            {@link EntityIdentityComparator} to create a {@link RefreshableSelectionModel}
     */
    public TableWrapper(SailingServiceAsync sailingService, final StringMessages stringMessages,
            ErrorReporter errorReporter, boolean multiSelection, boolean enablePager, int pagingSize,
            EntityIdentityComparator<T> entityIdentityComparator) {
        this.entityIdentityComparator = entityIdentityComparator;
        this.multiSelection = multiSelection;
        this.sailingService = sailingService;
        this.errorReporter = errorReporter;
        this.stringMessages = stringMessages;
        table = new FlushableCellTable<T>(10000, tableRes);
        table.ensureDebugId("WrappedTable");
        this.dataProvider = new ListDataProvider<T>();
        this.columnSortHandler = new ListHandler<T>(dataProvider.getList());
        table.addColumnSortHandler(this.columnSortHandler);
        registerSelectionModelOnNewDataProvider(dataProvider);
        mainPanel = new VerticalPanel();
        dataProvider.addDataDisplay(table);
        mainPanel.add(table);
        if (enablePager) {
            table.setPageSize(pagingSize);
            SimplePager pager = new SimplePager() {
                protected String createText() {
                    HasRows display = getDisplay();
                    Range range = display.getVisibleRange();
                    int pageStart = range.getStart() + 1;
                    int pageSize = range.getLength();
                    int dataSize = display.getRowCount();
                    int endIndex = Math.min(dataSize, pageStart + pageSize - 1);
                    endIndex = Math.max(pageStart, endIndex);
                    boolean exact = display.isRowCountExact();
                    return stringMessages.pagerStateInfo(pageStart, endIndex, dataSize, exact);
                }
            };
            pager.setDisplay(table);
            mainPanel.add(pager);
        }
    }
    
    public ListHandler<T> getColumnSortHandler() {
        return columnSortHandler;
    }

    public CellTable<T> getTable() {
        return table;
    }
    
    public S getSelectionModel() {
        return selectionModel;
    }
    
    public ListDataProvider<T> getDataProvider() {
        return dataProvider;
    }
    
    public void refresh(Iterable<T> newItems) {
        dataProvider.getList().clear();
        Util.addAll(newItems, dataProvider.getList());
        dataProvider.flush();
    }
    
    /**
     * This method allows you to change the data base for the {@link RefreshableSelectionModel}. This can, e.g., be useful
     * if the table wrapped by this object is filtered through an {@link AbstractFilterablePanel} that has an
     * {@link AbstractFilterablePanel#getAll() all} data structure of which the table displays only a subset, but selection
     * shall be kept across filtering, based on all records available so that removing the filter will restore the previous
     * selection again.
     * 
     * @param dataProvider
     *            {@link ListDataProvider} as data base for the {@link RefreshableSelectionModel}.
     */
    public void registerSelectionModelOnNewDataProvider(ListDataProvider<T> dataProvider) {
        this.dataProvider = dataProvider;
        if (multiSelection) {
            if (selectionCheckboxColumn != null) {
                table.removeColumn(selectionCheckboxColumn);
            }
            selectionCheckboxColumn = new SelectionCheckboxColumn<T>(
                    tableRes.cellTableStyle().cellTableCheckboxSelected(),
                    tableRes.cellTableStyle().cellTableCheckboxDeselected(),
                    tableRes.cellTableStyle().cellTableCheckboxColumnCell(), entityIdentityComparator, dataProvider,
                    table);
            columnSortHandler.setComparator(selectionCheckboxColumn, selectionCheckboxColumn.getComparator());
            @SuppressWarnings("unchecked")
            S typedSelectionModel = (S) selectionCheckboxColumn.getSelectionModel();
            selectionModel = typedSelectionModel;
            table.setSelectionModel(selectionModel, selectionCheckboxColumn.getSelectionManager());
            table.addColumn(selectionCheckboxColumn, selectionCheckboxColumn.getHeader());
        } else {
            @SuppressWarnings("unchecked")
            S typedSelectionModel = (S) new RefreshableSingleSelectionModel<T>(entityIdentityComparator, dataProvider);
            selectionModel = typedSelectionModel;
            table.setSelectionModel(selectionModel);
        }
    }
}