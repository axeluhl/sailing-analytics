package com.sap.sse.gwt.client.panels;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import com.google.gwt.user.cellview.client.AbstractCellTable;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.ListDataProvider;
import com.sap.sse.common.Util;

/**
 * Customizable filter panel which allows to create a flexible filter panel with various number of filter inputs (e.g.
 * combo box and text box). Need to set filter value on each input field change handler.
 * 
 * @param <T>
 * @author Oleg Zheleznov
 * 
 */
public abstract class CustomizableFilterablePanel<T> extends AbstractFilterablePanel<T> {
    public static interface Filter<T> {
        boolean matches(T t);
    }

    private Set<Filter<T>> filters;

    public CustomizableFilterablePanel(Iterable<T> all, AbstractCellTable<T> display, ListDataProvider<T> filtered) {
        super(all, display, filtered, /* show default filter text box */ false);
        this.filters = new HashSet<>();
    }

    public void add(Label label, Widget widget, Filter<T> filter) {
        add(label);
        add(widget);
        filters.add(filter);
        setCellVerticalAlignment(label, HasVerticalAlignment.ALIGN_MIDDLE);
    }

    @Override
    public void filter() {
        super.filter();
        boolean needRefresh = false;
        for (final Iterator<T> i = filtered.getList().iterator(); i.hasNext(); ) {
            final T t = i.next();
            for (final Filter<T> filter : filters) {
                if (!filter.matches(t)) {
                    i.remove();
                    needRefresh = true;
                    break;
                }
            }
        }
        if (needRefresh) {
            filtered.refresh();
        }
        // no additional sorting required because super.filter() has already sorted all entries, and removing will leave sorting stable
    }

    public void clearFilter() {
        filtered.getList().clear();
        Util.addAll(all.getList(), filtered.getList());
    }
}
