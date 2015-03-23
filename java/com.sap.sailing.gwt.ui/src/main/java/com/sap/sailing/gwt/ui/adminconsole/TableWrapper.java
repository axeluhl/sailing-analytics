package com.sap.sailing.gwt.ui.adminconsole;

import java.util.Collection;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.SimplePager;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.SelectionModel;
import com.google.gwt.view.client.SingleSelectionModel;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.client.shared.controls.SelectionCheckboxColumn;
import com.sap.sse.gwt.client.ErrorReporter;

/**
 * Implementing classes still have to add the table to the main panel.
 */
public abstract class TableWrapper<T, S extends SelectionModel<T>> implements IsWidget {
    protected final CellTable<T> table;
    private final S selectionModel;
    protected final ListDataProvider<T> dataProvider;
    protected VerticalPanel mainPanel;
    protected final SailingServiceAsync sailingService;
    protected final ErrorReporter errorReporter;

    private final AdminConsoleTableResources tableRes = GWT.create(AdminConsoleTableResources.class);
    
    @Override
    public Widget asWidget() {
        return mainPanel;
    }

    public TableWrapper(SailingServiceAsync sailingService, StringMessages stringMessages, ErrorReporter errorReporter,
            boolean multiSelection, boolean enablePager) {
        this.sailingService = sailingService;
        this.errorReporter = errorReporter;
        table = new CellTable<T>(10000, tableRes);
        if (multiSelection) {
            SelectionCheckboxColumn<T> selectionCheckboxColumn = new SelectionCheckboxColumn<T>(
                    tableRes.cellTableStyle().cellTableCheckboxSelected(),
                    tableRes.cellTableStyle().cellTableCheckboxDeselected(),
                    tableRes.cellTableStyle().cellTableCheckboxColumnCell()) {
                        @Override
                        protected ListDataProvider<T> getListDataProvider() {
                            return dataProvider;
                        }
            };
            @SuppressWarnings("unchecked")
            S typedSelectionModel = (S) selectionCheckboxColumn.getSelectionModel();
            selectionModel = typedSelectionModel;
            table.setSelectionModel(selectionModel, selectionCheckboxColumn.getSelectionManager());
            table.addColumn(selectionCheckboxColumn, selectionCheckboxColumn.getHeader());
        } else {
            @SuppressWarnings("unchecked")
            S typedSelectionModel = (S) new SingleSelectionModel<T>();
            selectionModel = typedSelectionModel;
            table.setSelectionModel(selectionModel);
        }
        this.dataProvider = new ListDataProvider<T>();
        mainPanel = new VerticalPanel();
        dataProvider.addDataDisplay(table);
        mainPanel.add(table);
        if (enablePager) {
            table.setPageSize(8);
            SimplePager pager = new SimplePager();
            pager.setDisplay(table);
            mainPanel.add(pager);
        }
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
    
    public void refresh(Collection<T> newItems) {
        dataProvider.getList().clear();
        dataProvider.getList().addAll(newItems);
        dataProvider.flush();
    }
}