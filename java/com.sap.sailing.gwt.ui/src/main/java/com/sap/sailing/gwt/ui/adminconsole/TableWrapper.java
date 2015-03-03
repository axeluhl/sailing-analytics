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
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
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
            S selectionModel, boolean enablePager) {
        this.sailingService = sailingService;
        this.errorReporter = errorReporter;
        this.selectionModel = selectionModel;
        this.dataProvider = new ListDataProvider<T>();
        mainPanel = new VerticalPanel();
        table = new CellTable<T>(10000, tableRes);
        dataProvider.addDataDisplay(table);
        table.setSelectionModel(selectionModel);
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