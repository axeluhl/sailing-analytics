package com.sap.sse.gwt.client.panels;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import com.google.gwt.user.cellview.client.AbstractCellTable;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.ListDataProvider;
import com.sap.sse.common.Util;
import com.sap.sse.common.filter.AbstractListFilter;

/**
 * Customizable filter panel which allows to create a flexible filter panel with various number of filter inputs (e.g.
 * combo box and text box). Need to set filter value on each input field change handler.
 * 
 * @param <T>
 * @author Oleg Zheleznov
 * 
 */
public abstract class CustomizableFilterablePanel<T> extends AbstractFilterablePanel<T> {

    private Map<Widget, AbstractListFilter<T>> widgetsFilterMap;

    public CustomizableFilterablePanel(Iterable<T> all, AbstractCellTable<T> display, ListDataProvider<T> filtered) {
        super(all, display, filtered, /* show default filter text box */ false);
        this.widgetsFilterMap = new HashMap<>();
    }

    private String getFilterValue(Widget widget) {
        return widget.getElement().getPropertyString("value");
    }

    public void add(Label label, Widget widget, AbstractListFilter<T> filterer) {
        widgetsFilterMap.put(widget, filterer);
        add(label);
        add(widget);
        setCellVerticalAlignment(label, HasVerticalAlignment.ALIGN_MIDDLE);
    }

    @Override
    public void filter() {
        super.filter();
        for (Widget widget : widgetsFilterMap.keySet()) {
            filtered.setList(Util.createList(widgetsFilterMap.get(widget).applyFilter(Arrays.asList(getFilterValue(widget)), filtered.getList())));
        }
        filtered.refresh();
        sort();
    }

    public void clearFilter() {
        filtered.getList().clear();
        Util.addAll(all.getList(), filtered.getList());
    }
}
