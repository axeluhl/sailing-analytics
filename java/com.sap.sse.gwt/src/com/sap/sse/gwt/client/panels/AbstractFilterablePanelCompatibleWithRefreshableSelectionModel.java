package com.sap.sse.gwt.client.panels;

import java.util.Arrays;

import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.user.cellview.client.AbstractCellTable;
import com.google.gwt.user.cellview.client.ColumnSortEvent;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.view.client.ListDataProvider;
import com.sap.sse.common.Util;
import com.sap.sse.common.filter.AbstractListFilter;
import com.sap.sse.gwt.client.panels.HasDataAdapter;

public abstract class AbstractFilterablePanelCompatibleWithRefreshableSelectionModel<T> extends HorizontalPanel {
    protected final ListDataProvider<T> buffer;
    protected final ListDataProvider<T> displayedData;
    protected final AbstractCellTable<T> table;
    protected final TextBox textBox;

    private final AbstractListFilter<T> filterer = new AbstractListFilter<T>() {
        @Override
        public Iterable<String> getStrings(T t) {
            return getSearchableStrings(t);
        }
    };

    public AbstractFilterablePanelCompatibleWithRefreshableSelectionModel(ListDataProvider<T> buffer,
            ListDataProvider<T> displayedData, AbstractCellTable<T> table) {
        setSpacing(5);
        this.table = table;
        this.buffer = buffer;
        this.displayedData = displayedData;
        this.textBox = new TextBox();
        textBox.ensureDebugId("FilterTextBox");
        this.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
        add(getTextBox());
        getTextBox().addKeyUpHandler(new KeyUpHandler() {
            @Override
            public void onKeyUp(KeyUpEvent event) {
                filter();
            }
        });
        this.buffer.addDataDisplay(new HasDataAdapter<>(this));
    }

    public abstract Iterable<String> getSearchableStrings(T t);

    public void filter() {
        displayedData.getList().clear();
        Util.addAll(filterer.applyFilter(Arrays.asList(getTextBox().getText().split(" ")), buffer.getList()),
                displayedData.getList());
        displayedData.refresh();
        sort();
    }

    private void sort() {
        ColumnSortEvent.fire(table, table.getColumnSortList());
    }

    public TextBox getTextBox() {
        return textBox;
    }

}
